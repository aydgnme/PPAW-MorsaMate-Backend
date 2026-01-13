package me.aydgn.MorseMate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.dto.request.UserSubscriptionRequest;
import me.aydgn.MorseMate.dto.response.UserSubscriptionResponse;
import me.aydgn.MorseMate.entity.SubscriptionPlan;
import me.aydgn.MorseMate.entity.User;
import me.aydgn.MorseMate.entity.UserSubscription;
import me.aydgn.MorseMate.enums.Role;
import me.aydgn.MorseMate.exception.ResourceNotFoundException;
import me.aydgn.MorseMate.repository.SubscriptionPlanRepository;
import me.aydgn.MorseMate.repository.UserRepository;
import me.aydgn.MorseMate.repository.UserSubscriptionRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Service for managing user subscriptions.
 * Handles subscription lifecycle: creation, cancellation, renewal, and
 * upgrades.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserSubscriptionService {

    private final UserSubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final SubscriptionPlanRepository planRepository;

    private static final String FREE_PLAN_NAME = "Free";

    /**
     * Helper to map subscription entity to response DTO with full plan details.
     */
    private UserSubscriptionResponse mapToResponse(UserSubscription subscription) {
        UserSubscriptionResponse response = UserSubscriptionResponse.from(subscription);

        if (subscription.getPlanId() != null) {
            planRepository.findById(subscription.getPlanId())
                    .ifPresent(plan -> response
                            .setPlan(me.aydgn.MorseMate.dto.response.SubscriptionPlanResponse.from(plan)));
        }

        return response;
    }

    /**
     * Get current active subscription for a user.
     *
     * @param userId User ID
     * @return UserSubscriptionResponse or null if no active subscription
     */
    @Cacheable(value = "user-subscriptions", key = "#userId")
    @Transactional(readOnly = true)
    public UserSubscriptionResponse getUserSubscription(Long userId) {
        log.debug("Fetching subscription for user ID: {}", userId);

        return subscriptionRepository.findByUserId(userId)
                .map(this::mapToResponse)
                .orElse(null);
    }

    /**
     * Subscribe user to a plan.
     * Creates new subscription if none exists, or replaces cancelled/expired ones.
     *
     * @param userId  User ID
     * @param request Subscription request with plan details
     * @return Created UserSubscriptionResponse
     * @throws ResourceNotFoundException if user or plan not found
     * @throws IllegalStateException     if user already has active subscription
     */
    @CacheEvict(value = "user-subscriptions", key = "#userId")
    @Transactional
    public UserSubscriptionResponse subscribeUser(Long userId, UserSubscriptionRequest request) {
        log.info("Creating subscription for user ID: {} to plan ID: {}", userId, request.getPlanId());

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Validate plan exists and is active
        SubscriptionPlan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subscription plan not found with ID: " + request.getPlanId()));

        if (!Boolean.TRUE.equals(plan.getIsActive())) {
            throw new IllegalStateException("Cannot subscribe to inactive plan: " + plan.getName());
        }

        // Check for existing active subscription
        UserSubscription existingSubscription = subscriptionRepository.findByUserId(userId).orElse(null);

        if (existingSubscription != null) {
            if (existingSubscription.getStatus() == UserSubscription.SubscriptionStatus.ACTIVE
                    && (existingSubscription.getCurrentPeriodEnd() == null
                            || existingSubscription.getCurrentPeriodEnd().isAfter(OffsetDateTime.now()))) {
                throw new IllegalStateException(
                        "User already has an active subscription. Please cancel or upgrade instead.");
            }
        }

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime endDate = calculateEndDate(now, plan.getBillingPeriod());

        String stripeCustomerId = user.getStripeCustomerId() != null ? user.getStripeCustomerId()
                : ("local-cus-" + user.getId());

        UserSubscription subscriptionToSave;

        if (existingSubscription != null) {
            // Update existing subscription
            subscriptionToSave = existingSubscription;
            subscriptionToSave.setPlanId(plan.getId());
            subscriptionToSave.setStatus(UserSubscription.SubscriptionStatus.ACTIVE);
            subscriptionToSave.setCurrentPeriodStart(now);
            subscriptionToSave.setCurrentPeriodEnd(endDate);
            subscriptionToSave.setStripeSubscriptionId(request.getPaymentMethodId()); // TODO: Integrate with actual
                                                                                      // Stripe
            subscriptionToSave.setStripeCustomerId(stripeCustomerId);
            subscriptionToSave.setAutoRenew(true);
        } else {
            // Create NEW subscription
            subscriptionToSave = UserSubscription.builder()
                    .user(user)
                    .planId(plan.getId())
                    .status(UserSubscription.SubscriptionStatus.ACTIVE)
                    .currentPeriodStart(now)
                    .currentPeriodEnd(endDate)
                    .stripeSubscriptionId(request.getPaymentMethodId()) // TODO: Integrate with actual Stripe
                    .stripeCustomerId(stripeCustomerId)
                    .autoRenew(true)
                    .build();
        }

        UserSubscription savedSubscription = subscriptionRepository.save(subscriptionToSave);
        log.info("Subscription created/updated successfully with ID: {}", savedSubscription.getId());

        // Update user role to PREMIUM
        updateUserRole(user, Role.PREMIUM);

        // Update user's maxHearts if plan specifies
        if (plan.getMaxHearts() != null && plan.getMaxHearts() > user.getMaxHearts()) {
            user.setMaxHearts(plan.getMaxHearts());
            userRepository.save(user);
            log.debug("Updated user {} maxHearts to {}", userId, plan.getMaxHearts());
        }

        return mapToResponse(savedSubscription);
    }

    /**
     * Cancel user's active subscription.
     *
     * @param userId User ID
     * @throws ResourceNotFoundException if no subscription found
     * @throws IllegalStateException     if subscription already cancelled/expired
     */
    @CacheEvict(value = "user-subscriptions", key = "#userId")
    @Transactional
    public void cancelSubscription(Long userId) {
        log.info("Cancelling subscription for user ID: {}", userId);

        UserSubscription subscription = subscriptionRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No subscription found for user ID: " + userId));

        if (subscription.getStatus() != UserSubscription.SubscriptionStatus.ACTIVE) {
            throw new IllegalStateException("Cannot cancel subscription with status: " + subscription.getStatus());
        }

        // Cancel subscription
        subscription.setStatus(UserSubscription.SubscriptionStatus.CANCELED);
        subscriptionRepository.save(subscription);

        // Revert user role to USER
        User user = subscription.getUser();
        updateUserRole(user, Role.USER);

        // Reset user's maxHearts to default (5)
        user.setMaxHearts(5);
        userRepository.save(user);

        log.info("Subscription cancelled successfully for user ID: {}", userId);
    }

    /**
     * Renew an expired subscription.
     *
     * @param subscriptionId Subscription ID
     * @return Renewed UserSubscriptionResponse
     * @throws ResourceNotFoundException if subscription not found
     * @throws IllegalStateException     if subscription is not expired or cancelled
     */
    @CacheEvict(value = "user-subscriptions", key = "#result.userId")
    @Transactional
    public UserSubscriptionResponse renewSubscription(Long subscriptionId) {
        log.info("Renewing subscription ID: {}", subscriptionId);

        UserSubscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with ID: " + subscriptionId));

        if (subscription.getStatus() != UserSubscription.SubscriptionStatus.INCOMPLETE
                && subscription.getStatus() != UserSubscription.SubscriptionStatus.CANCELED) {
            throw new IllegalStateException("Cannot renew subscription with status: " + subscription.getStatus());
        }

        SubscriptionPlan plan = planRepository.findById(Long.valueOf(subscription.getPlanId()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subscription plan not found with ID: " + subscription.getPlanId()));

        // Renew subscription
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime newEndDate = calculateEndDate(now, plan.getBillingPeriod());

        subscription.setStatus(UserSubscription.SubscriptionStatus.ACTIVE);
        subscription.setCurrentPeriodStart(now);
        subscription.setCurrentPeriodEnd(newEndDate);

        UserSubscription renewed = subscriptionRepository.save(subscription);

        // Update user role to PREMIUM
        updateUserRole(subscription.getUser(), Role.PREMIUM);

        // Update maxHearts
        if (plan.getMaxHearts() != null) {
            User user = subscription.getUser();
            user.setMaxHearts(plan.getMaxHearts());
            userRepository.save(user);
        }

        log.info("Subscription renewed successfully: {}", subscriptionId);
        return mapToResponse(renewed);
    }

    /**
     * Upgrade user's subscription to a different plan.
     *
     * @param userId    User ID
     * @param newPlanId New plan ID
     * @return Updated UserSubscriptionResponse
     * @throws ResourceNotFoundException if user, subscription, or plan not found
     * @throws IllegalStateException     if no active subscription or downgrade
     *                                   attempted
     */
    @CacheEvict(value = "user-subscriptions", key = "#userId")
    @Transactional
    public UserSubscriptionResponse upgradeSubscription(Long userId, Long newPlanId) {
        log.info("Upgrading subscription for user ID: {} to plan ID: {}", userId, newPlanId);

        // Get current subscription
        UserSubscription subscription = subscriptionRepository.findByUserId(userId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("No active subscription found for user ID: " + userId));

        SubscriptionPlan oldPlan = planRepository.findById(Long.valueOf(subscription.getPlanId()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subscription plan not found with ID: " + subscription.getPlanId()));

        // Get new plan
        SubscriptionPlan newPlan = planRepository.findById(newPlanId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription plan not found with ID: " + newPlanId));

        if (!Boolean.TRUE.equals(newPlan.getIsActive())) {
            throw new IllegalStateException("Cannot upgrade to inactive plan: " + newPlan.getName());
        }

        // Check if it's actually an upgrade (higher price)
        if (newPlan.getPrice().compareTo(oldPlan.getPrice()) < 0) {
            log.warn("Attempted downgrade for user {}. Current: {}, New: {}",
                    userId, oldPlan.getPrice(), newPlan.getPrice());
            throw new IllegalStateException("Downgrade not supported. Please cancel and subscribe to lower plan.");
        }

        // Update subscription plan
        subscription.setPlanId(newPlan.getId());

        // Update maxHearts if new plan has more
        if (newPlan.getMaxHearts() != null) {
            User user = subscription.getUser();
            user.setMaxHearts(newPlan.getMaxHearts());
            userRepository.save(user);
        }

        UserSubscription upgraded = subscriptionRepository.save(subscription);
        log.info("Subscription upgraded successfully for user ID: {}", userId);

        return mapToResponse(upgraded);
    }

    /**
     * Get subscription history for a user.
     *
     * @param userId User ID
     * @return List of all subscriptions (past and present)
     */
    @Transactional(readOnly = true)
    public List<UserSubscriptionResponse> getSubscriptionHistory(Long userId) {
        log.debug("Fetching subscription history for user ID: {}", userId);

        // For now, we only store one subscription per user
        // If history is needed, we'd need to modify the schema
        return subscriptionRepository.findByUserId(userId)
                .map(this::mapToResponse)
                .map(List::of)
                .orElse(List.of());
    }

    /**
     * Check if user has active premium subscription.
     *
     * @param userId User ID
     * @return true if user has active subscription
     */
    @Transactional(readOnly = true)
    public boolean isUserPremium(Long userId) {
        return subscriptionRepository.isUserPremium(userId);
    }

    /**
     * Ensure a user has a Free subscription on signup. Creates the Free plan if
     * missing.
     * Does NOT grant premium role or alter user hearts beyond plan defaults.
     */
    @Transactional
    public void ensureFreeSubscriptionOnSignup(User user) {
        if (user == null || user.getId() == null)
            return;

        // If user already has a subscription record, skip creating another
        if (subscriptionRepository.findByUserId(user.getId()).isPresent()) {
            return;
        }

        // Get or create the Free plan (price = 0, active)
        SubscriptionPlan freePlan = planRepository.findByNameIgnoreCase(FREE_PLAN_NAME)
                .orElseGet(() -> {
                    SubscriptionPlan plan = SubscriptionPlan.builder()
                            .name(FREE_PLAN_NAME)
                            .description("Default free tier with basic features")
                            .price(java.math.BigDecimal.ZERO)
                            .billingPeriod(SubscriptionPlan.BillingPeriod.MONTHLY)
                            .maxHearts(5)
                            .isActive(true)
                            .build();
                    return planRepository.save(plan);
                });

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime end = now.plusMonths(1); // rotate monthly for bookkeeping

        String stripeCustomerId = user.getStripeCustomerId() != null ? user.getStripeCustomerId()
                : ("free-cus-" + user.getId());

        UserSubscription subscription = UserSubscription.builder()
                .user(user)
                .stripeSubscriptionId("free-" + user.getId())
                .stripeCustomerId(stripeCustomerId)
                .planId(freePlan.getId())
                .status(UserSubscription.SubscriptionStatus.ACTIVE)
                .currentPeriodStart(now)
                .currentPeriodEnd(end)
                .build();

        subscriptionRepository.save(subscription);
    }

    /**
     * Calculate subscription end date based on billing period.
     *
     * @param startDate     Start date
     * @param billingPeriod Billing period (MONTHLY, YEARLY, etc.)
     * @return Calculated end date
     */
    private OffsetDateTime calculateEndDate(OffsetDateTime startDate, SubscriptionPlan.BillingPeriod billingPeriod) {
        if (billingPeriod == null) {
            return null; // Lifetime subscription
        }

        return switch (billingPeriod) {
            case MONTHLY -> startDate.plusMonths(1);
            case YEARLY -> startDate.plusYears(1);
            case LIFETIME -> null;
        };
    }

    /**
     * Update user's role.
     *
     * @param user User entity
     * @param role New role
     */
    private void updateUserRole(User user, Role role) {
        if (user.getRole() != role) {
            user.setRole(role);
            userRepository.save(user);
            log.debug("Updated user {} role to {}", user.getId(), role);
        }
    }
}
