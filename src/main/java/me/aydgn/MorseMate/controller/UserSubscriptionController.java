package me.aydgn.MorseMate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.dto.request.UserSubscriptionRequest;
import me.aydgn.MorseMate.dto.response.ApiMessage;
import me.aydgn.MorseMate.dto.response.UserSubscriptionResponse;
import me.aydgn.MorseMate.service.UserSubscriptionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for user subscription management.
 * Handles subscription operations: view, subscribe, cancel, upgrade, and history.
 */
@RestController
@RequestMapping("/${api.version}/subscriptions")
@RequiredArgsConstructor
@Slf4j
public class UserSubscriptionController {

    private final UserSubscriptionService subscriptionService;

    /**
     * Get current user's subscription.
     * Returns null if user has no subscription.
     *
     * @return Current subscription or null
     */
    @GetMapping("/my")
    public ResponseEntity<UserSubscriptionResponse> getMySubscription() {
        Long userId = getCurrentUserId();
        log.debug("Fetching subscription for current user ID: {}", userId);

        UserSubscriptionResponse subscription = subscriptionService.getUserSubscription(userId);

        if (subscription == null) {
            log.debug("No subscription found for user ID: {}", userId);
            return ResponseEntity.ok(null);
        }

        return ResponseEntity.ok(subscription);
    }

    /**
     * Subscribe current user to a plan.
     * Creates new subscription if none exists.
     *
     * @param request Subscription request with plan details
     * @return Created subscription
     */
    @PostMapping("/subscribe")
    public ResponseEntity<UserSubscriptionResponse> subscribe(
            @Valid @RequestBody UserSubscriptionRequest request) {

        Long userId = getCurrentUserId();
        log.info("User ID: {} subscribing to plan ID: {}", userId, request.getPlanId());

        UserSubscriptionResponse subscription = subscriptionService.subscribeUser(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(subscription);
    }

    /**
     * Cancel current user's subscription.
     * Sets subscription status to CANCELLED and reverts user to basic tier.
     *
     * @return Success message
     */
    @DeleteMapping("/cancel")
    public ResponseEntity<ApiMessage> cancelSubscription() {
        Long userId = getCurrentUserId();
        log.info("User ID: {} cancelling subscription", userId);

        subscriptionService.cancelSubscription(userId);

        return ResponseEntity.ok(ApiMessage.of("Subscription cancelled successfully"));
    }

    /**
     * Get current user's subscription history.
     * Returns all past and present subscriptions.
     *
     * @return List of subscriptions
     */
    @GetMapping("/history")
    public ResponseEntity<List<UserSubscriptionResponse>> getSubscriptionHistory() {
        Long userId = getCurrentUserId();
        log.debug("Fetching subscription history for user ID: {}", userId);

        List<UserSubscriptionResponse> history = subscriptionService.getSubscriptionHistory(userId);
        return ResponseEntity.ok(history);
    }

    /**
     * Upgrade current user's subscription to a different plan.
     * Only allows upgrades (higher price plans).
     *
     * @param newPlanId New subscription plan ID
     * @return Updated subscription
     */
    @PutMapping("/upgrade")
    public ResponseEntity<UserSubscriptionResponse> upgradeSubscription(@RequestParam Long newPlanId) {
        Long userId = getCurrentUserId();
        log.info("User ID: {} upgrading to plan ID: {}", userId, newPlanId);

        UserSubscriptionResponse subscription = subscriptionService.upgradeSubscription(userId, newPlanId);
        return ResponseEntity.ok(subscription);
    }

    /**
     * Renew an expired or cancelled subscription.
     * Available to subscription owner or admin.
     *
     * @param subscriptionId Subscription ID to renew
     * @return Renewed subscription
     */
    @PostMapping("/renew/{subscriptionId}")
    public ResponseEntity<UserSubscriptionResponse> renewSubscription(
            @PathVariable("subscriptionId") Long subscriptionId) {

        log.info("Renewing subscription ID: {}", subscriptionId);

        UserSubscriptionResponse subscription = subscriptionService.renewSubscription(subscriptionId);
        return ResponseEntity.ok(subscription);
    }

    /**
     * Check if current user has premium subscription.
     *
     * @return Premium status
     */
    @GetMapping("/premium-status")
    public ResponseEntity<ApiMessage> checkPremiumStatus() {
        Long userId = getCurrentUserId();
        boolean isPremium = subscriptionService.isUserPremium(userId);

        String message = isPremium ? "User has active premium subscription" : "User does not have premium subscription";
        return ResponseEntity.ok(ApiMessage.of(message));
    }

    // ========== ADMIN ENDPOINTS ==========

    /**
     * Get subscription for any user (Admin only).
     *
     * @param userId User ID
     * @return User's subscription
     */
    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserSubscriptionResponse> getUserSubscription(@PathVariable("userId") Long userId) {
        log.debug("Admin fetching subscription for user ID: {}", userId);

        UserSubscriptionResponse subscription = subscriptionService.getUserSubscription(userId);

        if (subscription == null) {
            return ResponseEntity.ok(null);
        }

        return ResponseEntity.ok(subscription);
    }

    /**
     * Cancel subscription for any user (Admin only).
     *
     * @param userId User ID
     * @return Success message
     */
    @DeleteMapping("/users/{userId}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiMessage> cancelUserSubscription(@PathVariable("userId") Long userId) {
        log.info("Admin cancelling subscription for user ID: {}", userId);

        subscriptionService.cancelSubscription(userId);

        return ResponseEntity.ok(ApiMessage.of("User subscription cancelled successfully"));
    }

    /**
     * Create subscription for any user (Admin only).
     * Allows admin to manually subscribe users.
     *
     * @param userId User ID
     * @param request Subscription request
     * @return Created subscription
     */
    @PostMapping("/users/{userId}/subscribe")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserSubscriptionResponse> subscribeUser(
            @PathVariable("userId") Long userId,
            @Valid @RequestBody UserSubscriptionRequest request) {

        log.info("Admin subscribing user ID: {} to plan ID: {}", userId, request.getPlanId());

        UserSubscriptionResponse subscription = subscriptionService.subscribeUser(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(subscription);
    }

    // ========== HELPER METHODS ==========

    /**
     * Get current user ID from security context.
     *
     * @return Current user ID
     * @throws IllegalStateException if user is not authenticated
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }
        return Long.parseLong(authentication.getName());
    }
}
