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

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing user subscriptions.
 * Handles subscription lifecycle: creation, cancellation, renewal, and upgrades.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserSubscriptionService {

    private final UserSubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final SubscriptionPlanRepository planRepository;

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

        return subscriptionRepository.findWithPlanByUserId(userId)
                .map(UserSubscriptionResponse::from)
                .orElse(null);
    }

    /**
     * Subscribe user to a plan.
     * Creates new subscription if none exists, or replaces cancelled/expired ones.
     *
     * @param userId User ID
     * @param request Subscription request with plan details
     * @return Created UserSubscriptionResponse
     * @throws ResourceNotFoundException if user or plan not found
     * @throws IllegalStateException if user already has active subscription
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
                .orElseThrow(() -> new ResourceNotFoundException("Subscription plan not found with ID: " + request.getPlanId()));

        if (!Boolean.TRUE.equals(plan.getIsActive())) {
            throw new IllegalStateException("Cannot subscribe to inactive plan: " + plan.getName());
        }

        // Check for existing active subscription
        subscriptionRepository.findByUserId(userId).ifPresent(existing -> {
            if (existing.getStatus() == UserSubscription.Status.ACTIVE
                    && (existing.getEndDate() == null || existing.getEndDate().isAfter(LocalDateTime.now()))) {
                throw new IllegalStateException("User already has an active subscription. Please cancel or upgrade instead.");
            }
        });

        // Calculate subscription dates
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDate = calculateEndDate(now, plan.getBillingPeriod());
        LocalDateTime nextBillingDate = plan.getBillingPeriod() != null ? endDate : null;

        // Create subscription
        UserSubscription subscription = UserSubscription.builder()
                .user(user)
                .plan(plan)
                .status(UserSubscription.Status.ACTIVE)
                .startDate(now)
                .endDate(endDate)
                .nextBillingDate(nextBillingDate)
                .autoRenew(request.getAutoRenew() != null ? request.getAutoRenew() : true)
                .stripeSubscriptionId(request.getPaymentMethodId()) // TODO: Integrate with actual Stripe
                .build();

        UserSubscription savedSubscription = subscriptionRepository.save(subscription);
        log.info("Subscription created successfully with ID: {}", savedSubscription.getId());

        // Update user role to PREMIUM
        updateUserRole(user, Role.PREMIUM);

        // Update user's maxHearts if plan specifies
        if (plan.getMaxHearts() != null && plan.getMaxHearts() > user.getMaxHearts()) {
            user.setMaxHearts(plan.getMaxHearts());
            userRepository.save(user);
            log.debug("Updated user {} maxHearts to {}", userId, plan.getMaxHearts());
        }

        return UserSubscriptionResponse.from(savedSubscription);
    }

    /**
     * Cancel user's active subscription.
     *
     * @param userId User ID
     * @throws ResourceNotFoundException if no subscription found
     * @throws IllegalStateException if subscription already cancelled/expired
     */
    @CacheEvict(value = "user-subscriptions", key = "#userId")
    @Transactional
    public void cancelSubscription(Long userId) {
        log.info("Cancelling subscription for user ID: {}", userId);

        UserSubscription subscription = subscriptionRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No subscription found for user ID: " + userId));

        if (subscription.getStatus() != UserSubscription.Status.ACTIVE) {
            throw new IllegalStateException("Cannot cancel subscription with status: " + subscription.getStatus());
        }

        // Cancel subscription
        subscription.setStatus(UserSubscription.Status.CANCELLED);
        subscription.setAutoRenew(false);
        subscription.setUpdatedAt(LocalDateTime.now());
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
     * @throws IllegalStateException if subscription is not expired or cancelled
     */
    @CacheEvict(value = "user-subscriptions", key = "#result.userId")
    @Transactional
    public UserSubscriptionResponse renewSubscription(Long subscriptionId) {
        log.info("Renewing subscription ID: {}", subscriptionId);

        UserSubscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with ID: " + subscriptionId));

        if (subscription.getStatus() != UserSubscription.Status.EXPIRED
                && subscription.getStatus() != UserSubscription.Status.CANCELLED) {
            throw new IllegalStateException("Cannot renew subscription with status: " + subscription.getStatus());
        }

        // Renew subscription
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime newEndDate = calculateEndDate(now, subscription.getPlan().getBillingPeriod());

        subscription.setStatus(UserSubscription.Status.ACTIVE);
        subscription.setStartDate(now);
        subscription.setEndDate(newEndDate);
        subscription.setNextBillingDate(newEndDate);
        subscription.setAutoRenew(true);
        subscription.setUpdatedAt(now);

        UserSubscription renewed = subscriptionRepository.save(subscription);

        // Update user role to PREMIUM
        updateUserRole(subscription.getUser(), Role.PREMIUM);

        // Update maxHearts
        if (subscription.getPlan().getMaxHearts() != null) {
            User user = subscription.getUser();
            user.setMaxHearts(subscription.getPlan().getMaxHearts());
            userRepository.save(user);
        }

        log.info("Subscription renewed successfully: {}", subscriptionId);
        return UserSubscriptionResponse.from(renewed);
    }

    /**
     * Upgrade user's subscription to a different plan.
     *
     * @param userId User ID
     * @param newPlanId New plan ID
     * @return Updated UserSubscriptionResponse
     * @throws ResourceNotFoundException if user, subscription, or plan not found
     * @throws IllegalStateException if no active subscription or downgrade attempted
     */
    @CacheEvict(value = "user-subscriptions", key = "#userId")
    @Transactional
    public UserSubscriptionResponse upgradeSubscription(Long userId, Long newPlanId) {
        log.info("Upgrading subscription for user ID: {} to plan ID: {}", userId, newPlanId);

        // Get current subscription
        UserSubscription subscription = subscriptionRepository.findWithPlanByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No active subscription found for user ID: " + userId));

        if (subscription.getStatus() != UserSubscription.Status.ACTIVE) {
            throw new IllegalStateException("Cannot upgrade inactive subscription");
        }

        // Get new plan
        SubscriptionPlan newPlan = planRepository.findById(newPlanId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription plan not found with ID: " + newPlanId));

        if (!Boolean.TRUE.equals(newPlan.getIsActive())) {
            throw new IllegalStateException("Cannot upgrade to inactive plan: " + newPlan.getName());
        }

        // Check if it's actually an upgrade (higher price)
        if (newPlan.getPrice().compareTo(subscription.getPlan().getPrice()) < 0) {
            log.warn("Attempted downgrade for user {}. Current: {}, New: {}",
                    userId, subscription.getPlan().getPrice(), newPlan.getPrice());
            throw new IllegalStateException("Downgrade not supported. Please cancel and subscribe to lower plan.");
        }

        // Update subscription plan
        subscription.setPlan(newPlan);
        subscription.setUpdatedAt(LocalDateTime.now());

        // Update maxHearts if new plan has more
        if (newPlan.getMaxHearts() != null) {
            User user = subscription.getUser();
            user.setMaxHearts(newPlan.getMaxHearts());
            userRepository.save(user);
        }

        UserSubscription upgraded = subscriptionRepository.save(subscription);
        log.info("Subscription upgraded successfully for user ID: {}", userId);

        return UserSubscriptionResponse.from(upgraded);
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
                .map(UserSubscriptionResponse::from)
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
     * Calculate subscription end date based on billing period.
     *
     * @param startDate Start date
     * @param billingPeriod Billing period (MONTHLY, YEARLY, etc.)
     * @return Calculated end date
     */
    private LocalDateTime calculateEndDate(LocalDateTime startDate, SubscriptionPlan.BillingPeriod billingPeriod) {
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
