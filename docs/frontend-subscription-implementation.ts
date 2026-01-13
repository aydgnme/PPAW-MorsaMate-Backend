// MorseMate Frontend - Subscription Implementation
// TypeScript + React + Axios
// Last updated: 2025-12-18

import axios, { AxiosError } from 'axios';
import { useState, useCallback } from 'react';

// ========== Types ==========

export enum SubscriptionStatus {
  ACTIVE = 'ACTIVE',
  CANCELED = 'CANCELED',
  INCOMPLETE = 'INCOMPLETE',
  PAST_DUE = 'PAST_DUE',
  TRIALING = 'TRIALING',
}

export interface SubscriptionPlan {
  id: number;
  name: string;
  displayName: string;
  description: string;
  price: number;
  currency: string;
  billingPeriod: string; // "MONTHLY" | "YEARLY"
  maxHearts: number;
  features: string[];
  isActive: boolean;
}

export interface UserSubscription {
  id: number;
  userId: number;
  planId: number;
  status: SubscriptionStatus;
  currentPeriodStart: string; // ISO date
  currentPeriodEnd: string; // ISO date
  stripeSubscriptionId?: string;
  stripeCustomerId?: string;
  autoRenew: boolean;
  plan?: SubscriptionPlan;
}

export interface SubscribeRequest {
  planId: number;
  paymentMethodId?: string;
}

export interface ApiError {
  message: string;
  errors?: Record<string, string>;
}

// ========== API Client ==========

const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/v1',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add auth token to requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('authToken'); // or your auth storage
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export class SubscriptionAPI {
  // Get all available plans
  static async getPlans(): Promise<SubscriptionPlan[]> {
    const { data } = await api.get<SubscriptionPlan[]>('/subscription-plans');
    return data;
  }

  // Get current user's subscription
  static async getCurrentSubscription(): Promise<UserSubscription | null> {
    try {
      const { data } = await api.get<UserSubscription>('/subscriptions/me');
      return data;
    } catch (error) {
      if (axios.isAxiosError(error) && error.response?.status === 404) {
        return null; // No subscription found
      }
      throw error;
    }
  }

  // Subscribe to a plan
  static async subscribe(request: SubscribeRequest): Promise<UserSubscription> {
    const { data } = await api.post<UserSubscription>('/subscriptions/subscribe', request);
    return data;
  }

  // Upgrade to a new plan
  static async upgrade(newPlanId: number): Promise<UserSubscription> {
    const { data } = await api.put<UserSubscription>('/subscriptions/upgrade', null, {
      params: { newPlanId },
    });
    return data;
  }

  // Cancel subscription
  static async cancel(): Promise<void> {
    await api.delete('/subscriptions/cancel');
  }

  // Check if subscription is currently active
  static isSubscriptionActive(subscription: UserSubscription | null): boolean {
    if (!subscription) return false;
    
    const isActive = subscription.status === SubscriptionStatus.ACTIVE;
    const notExpired = !subscription.currentPeriodEnd || 
      new Date(subscription.currentPeriodEnd) > new Date();
    
    return isActive && notExpired;
  }
}

// ========== Smart Change Plan Function ==========

export interface ChangePlanResult {
  success: boolean;
  message: string;
  subscription?: UserSubscription;
  action?: 'upgraded' | 'subscribed';
}

export async function changePlan(
  newPlanId: number,
  paymentMethodId?: string
): Promise<ChangePlanResult> {
  try {
    // 1. Get current subscription
    const currentSubscription = await SubscriptionAPI.getCurrentSubscription();
    const isActive = SubscriptionAPI.isSubscriptionActive(currentSubscription);

    // 2. Choose correct action
    if (isActive) {
      // Active subscription → upgrade
      const subscription = await SubscriptionAPI.upgrade(newPlanId);
      return {
        success: true,
        message: 'Plan upgraded successfully!',
        subscription,
        action: 'upgraded',
      };
    } else {
      // No active subscription → subscribe
      const subscription = await SubscriptionAPI.subscribe({
        planId: newPlanId,
        paymentMethodId,
      });
      return {
        success: true,
        message: 'Subscription created successfully!',
        subscription,
        action: 'subscribed',
      };
    }
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const status = error.response?.status;
      const apiError = error.response?.data as ApiError | undefined;
      const message = apiError?.message || 'An error occurred';

      // Handle "already has active subscription" error
      if (status === 400 && message.toLowerCase().includes('active subscription')) {
        try {
          // Retry with upgrade
          const subscription = await SubscriptionAPI.upgrade(newPlanId);
          return {
            success: true,
            message: 'Plan upgraded successfully!',
            subscription,
            action: 'upgraded',
          };
        } catch (retryError) {
          if (axios.isAxiosError(retryError)) {
            const retryMessage = (retryError.response?.data as ApiError)?.message || 
              'Upgrade failed. Please try again.';
            return { success: false, message: retryMessage };
          }
          return { success: false, message: 'Upgrade failed. Please try again.' };
        }
      }

      // Handle downgrade not supported
      if (status === 400 && message.toLowerCase().includes('downgrade')) {
        return {
          success: false,
          message: 'Downgrade not supported. Choose a higher plan or cancel first.',
        };
      }

      // Other errors
      return { success: false, message };
    }

    return { success: false, message: 'Something went wrong. Please try again.' };
  }
}

// ========== React Hooks ==========

export interface UseSubscriptionReturn {
  currentSubscription: UserSubscription | null;
  plans: SubscriptionPlan[];
  loading: boolean;
  error: string | null;
  isActive: boolean;
  changePlan: (planId: number, paymentMethodId?: string) => Promise<ChangePlanResult>;
  cancelSubscription: () => Promise<void>;
  refresh: () => Promise<void>;
}

export function useSubscription(): UseSubscriptionReturn {
  const [currentSubscription, setCurrentSubscription] = useState<UserSubscription | null>(null);
  const [plans, setPlans] = useState<SubscriptionPlan[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const isActive = SubscriptionAPI.isSubscriptionActive(currentSubscription);

  // Load initial data
  const refresh = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);

      const [subscription, plansData] = await Promise.all([
        SubscriptionAPI.getCurrentSubscription(),
        SubscriptionAPI.getPlans(),
      ]);

      setCurrentSubscription(subscription);
      setPlans(plansData.filter(p => p.isActive));
    } catch (err) {
      const message = axios.isAxiosError(err)
        ? (err.response?.data as ApiError)?.message || 'Failed to load subscription data'
        : 'Failed to load subscription data';
      setError(message);
    } finally {
      setLoading(false);
    }
  }, []);

  // Change plan wrapper
  const handleChangePlan = useCallback(
    async (planId: number, paymentMethodId?: string): Promise<ChangePlanResult> => {
      const result = await changePlan(planId, paymentMethodId);
      if (result.success) {
        setCurrentSubscription(result.subscription || null);
      }
      return result;
    },
    []
  );

  // Cancel subscription
  const handleCancelSubscription = useCallback(async () => {
    await SubscriptionAPI.cancel();
    await refresh();
  }, [refresh]);

  // Load on mount
  useState(() => {
    refresh();
  });

  return {
    currentSubscription,
    plans,
    loading,
    error,
    isActive,
    changePlan: handleChangePlan,
    cancelSubscription: handleCancelSubscription,
    refresh,
  };
}

// ========== Example React Component ==========

/*
import React from 'react';
import { useSubscription } from './subscription-api';

export function SubscriptionPlans() {
  const { plans, currentSubscription, isActive, loading, changePlan } = useSubscription();
  const [selectedPlanId, setSelectedPlanId] = React.useState<number | null>(null);
  const [message, setMessage] = React.useState<{ text: string; type: 'success' | 'error' } | null>(null);

  const handleSelectPlan = async (planId: number) => {
    setSelectedPlanId(planId);
    setMessage(null);

    const result = await changePlan(planId, 'pm_test_123'); // Use real payment method
    
    setMessage({
      text: result.message,
      type: result.success ? 'success' : 'error',
    });
    
    setSelectedPlanId(null);
  };

  if (loading) return <div>Loading...</div>;

  return (
    <div className="subscription-plans">
      <h2>Choose Your Plan</h2>
      
      {message && (
        <div className={`alert alert-${message.type}`}>
          {message.text}
        </div>
      )}

      <div className="plans-grid">
        {plans.map((plan) => {
          const isCurrent = currentSubscription?.planId === plan.id;
          const buttonText = isCurrent 
            ? 'Current Plan' 
            : isActive 
              ? 'Upgrade to This Plan' 
              : 'Subscribe';

          return (
            <div key={plan.id} className={`plan-card ${isCurrent ? 'current' : ''}`}>
              <h3>{plan.displayName}</h3>
              <p className="price">
                ${plan.price} / {plan.billingPeriod.toLowerCase()}
              </p>
              <p className="description">{plan.description}</p>
              
              <ul className="features">
                {plan.features.map((feature, idx) => (
                  <li key={idx}>✓ {feature}</li>
                ))}
              </ul>

              <button
                onClick={() => handleSelectPlan(plan.id)}
                disabled={isCurrent || selectedPlanId === plan.id}
                className={`btn ${isActive ? 'btn-upgrade' : 'btn-primary'}`}
              >
                {selectedPlanId === plan.id ? 'Processing...' : buttonText}
              </button>
            </div>
          );
        })}
      </div>
    </div>
  );
}
*/

// ========== Vanilla JS / Non-React Usage ==========

/*
// Example without React
async function handlePlanChange(planId: number) {
  try {
    const result = await changePlan(planId, 'pm_test_123');
    
    if (result.success) {
      alert(result.message); // or show toast
      // Reload UI
      window.location.reload();
    } else {
      alert(`Error: ${result.message}`);
    }
  } catch (error) {
    alert('An unexpected error occurred');
    console.error(error);
  }
}

// Load plans
async function loadPlans() {
  const plans = await SubscriptionAPI.getPlans();
  const current = await SubscriptionAPI.getCurrentSubscription();
  const isActive = SubscriptionAPI.isSubscriptionActive(current);
  
  // Render UI
  renderPlans(plans, current, isActive);
}
*/
