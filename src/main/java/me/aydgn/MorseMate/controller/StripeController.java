package me.aydgn.MorseMate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.dto.request.CreatePaymentIntentRequest;
import me.aydgn.MorseMate.dto.response.ApiMessage;
import me.aydgn.MorseMate.dto.response.PaymentIntentResponse;
import me.aydgn.MorseMate.dto.response.StripeCustomerResponse;
import me.aydgn.MorseMate.service.StripeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * REST controller for Stripe integration (simulated).
 *
 * SIMULATION MODE:
 * This controller handles simulated Stripe operations for testing and development.
 * All Stripe API calls are mocked without actual Stripe integration.
 * Real Stripe integration will be added in future iterations.
 *
 * Endpoints:
 * - POST   /v1/stripe/create-payment-intent   - Create Payment Intent
 * - POST   /v1/stripe/confirm-payment-intent  - Confirm Payment Intent
 * - POST   /v1/stripe/cancel-payment-intent   - Cancel Payment Intent
 * - GET    /v1/stripe/payment-intent/{id}     - Retrieve Payment Intent
 * - GET    /v1/stripe/customer                - Get customer info
 * - POST   /v1/stripe/webhook                 - Webhook handler (simulated)
 * - GET    /v1/stripe/stats                   - Simulation statistics (admin)
 * - DELETE /v1/stripe/clear                   - Clear simulated data (admin)
 */
@RestController
@RequestMapping("/${api.version}/stripe")
@RequiredArgsConstructor
@Slf4j
public class StripeController {

    private final StripeService stripeService;

    /**
     * Create a Payment Intent (simulated).
     *
     * Example request:
     * POST /v1/stripe/create-payment-intent
     * {
     *   "amount": 9.99,
     *   "currency": "usd",
     *   "planId": 1,
     *   "simulateFailure": false
     * }
     *
     * @param request Payment Intent creation request
     * @return Created Payment Intent with client secret
     */
    @PostMapping("/create-payment-intent")
    public ResponseEntity<PaymentIntentResponse> createPaymentIntent(
            @Valid @RequestBody CreatePaymentIntentRequest request) {

        Long userId = getCurrentUserId();
        log.info("User ID: {} creating Payment Intent for amount: {} {}",
                userId, request.getAmount(), request.getCurrency());

        PaymentIntentResponse paymentIntent = stripeService.createPaymentIntent(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentIntent);
    }

    /**
     * Confirm a Payment Intent (simulated).
     *
     * In real Stripe, this is usually done on the frontend using Stripe.js.
     * This endpoint simulates the confirmation process.
     *
     * @param paymentIntentId Payment Intent ID
     * @return Updated Payment Intent
     */
    @PostMapping("/confirm-payment-intent/{id}")
    public ResponseEntity<PaymentIntentResponse> confirmPaymentIntent(
            @PathVariable("id") String paymentIntentId) {

        log.info("Confirming Payment Intent: {}", paymentIntentId);

        PaymentIntentResponse paymentIntent = stripeService.confirmPaymentIntent(paymentIntentId);
        return ResponseEntity.ok(paymentIntent);
    }

    /**
     * Cancel a Payment Intent (simulated).
     *
     * @param paymentIntentId Payment Intent ID
     * @return Updated Payment Intent
     */
    @PostMapping("/cancel-payment-intent/{id}")
    public ResponseEntity<PaymentIntentResponse> cancelPaymentIntent(
            @PathVariable("id") String paymentIntentId) {

        log.info("Canceling Payment Intent: {}", paymentIntentId);

        PaymentIntentResponse paymentIntent = stripeService.cancelPaymentIntent(paymentIntentId);
        return ResponseEntity.ok(paymentIntent);
    }

    /**
     * Retrieve a Payment Intent (simulated).
     *
     * @param paymentIntentId Payment Intent ID
     * @return Payment Intent
     */
    @GetMapping("/payment-intent/{id}")
    public ResponseEntity<PaymentIntentResponse> getPaymentIntent(
            @PathVariable("id") String paymentIntentId) {

        log.debug("Retrieving Payment Intent: {}", paymentIntentId);

        PaymentIntentResponse paymentIntent = stripeService.retrievePaymentIntent(paymentIntentId);
        return ResponseEntity.ok(paymentIntent);
    }

    /**
     * Get current user's Stripe Customer info (simulated).
     *
     * @return Stripe Customer
     */
    @GetMapping("/customer")
    public ResponseEntity<StripeCustomerResponse> getCustomer() {
        Long userId = getCurrentUserId();
        log.debug("Fetching Stripe customer for user ID: {}", userId);

        StripeCustomerResponse customer = stripeService.getCustomerByUserId(userId);

        if (customer == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(customer);
    }

    /**
     * Webhook handler (simulated).
     *
     * In real Stripe, this endpoint receives webhook events from Stripe.
     * This simulated version allows testing webhook event handling.
     *
     * Supported event types:
     * - payment_intent.succeeded
     * - payment_intent.payment_failed
     * - payment_intent.canceled
     * - customer.created
     * - charge.refunded
     *
     * Example simulated webhook:
     * POST /v1/stripe/webhook
     * {
     *   "type": "payment_intent.succeeded",
     *   "data": {
     *     "object": {
     *       "id": "pi_sim_xxx",
     *       "amount": 999,
     *       "currency": "usd",
     *       "status": "succeeded"
     *     }
     *   }
     * }
     *
     * @param payload Webhook event payload
     * @return Success message
     */
    @PostMapping("/webhook")
    public ResponseEntity<ApiMessage> handleWebhook(@RequestBody Map<String, Object> payload) {
        log.info("Received simulated Stripe webhook: {}", payload.get("type"));

        // In real Stripe, you would verify the webhook signature here
        // Stripe-Signature header validation would be done

        String eventType = (String) payload.get("type");
        log.info("Processing webhook event type: {}", eventType);

        // Handle different event types
        switch (eventType) {
            case "payment_intent.succeeded":
                log.info("Payment Intent succeeded webhook received");
                // In real app, you would update your database, send confirmation emails, etc.
                break;

            case "payment_intent.payment_failed":
                log.warn("Payment Intent failed webhook received");
                // Handle failed payment
                break;

            case "payment_intent.canceled":
                log.info("Payment Intent canceled webhook received");
                // Handle canceled payment
                break;

            case "customer.created":
                log.info("Customer created webhook received");
                break;

            case "charge.refunded":
                log.info("Charge refunded webhook received");
                // Handle refund
                break;

            default:
                log.debug("Unhandled webhook event type: {}", eventType);
        }

        return ResponseEntity.ok(ApiMessage.of("Webhook processed successfully"));
    }

    /**
     * Trigger a simulated webhook event (for testing).
     *
     * This endpoint allows you to manually trigger webhook events for testing.
     *
     * @param eventType Event type (e.g., payment_intent.succeeded)
     * @param paymentIntentId Payment Intent ID
     * @return Webhook event data
     */
    @PostMapping("/simulate-webhook")
    public ResponseEntity<Map<String, Object>> simulateWebhook(
            @RequestParam String eventType,
            @RequestParam String paymentIntentId) {

        log.info("Simulating webhook event: {} for Payment Intent: {}", eventType, paymentIntentId);

        Map<String, Object> event = stripeService.simulateWebhookEvent(eventType, paymentIntentId);
        return ResponseEntity.ok(event);
    }

    // ========== ADMIN ENDPOINTS ==========

    /**
     * Get Stripe simulation statistics (admin only).
     *
     * @return Simulation statistics
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getSimulationStats() {
        log.debug("Admin fetching Stripe simulation statistics");

        Map<String, Object> stats = stripeService.getSimulationStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Clear all simulated Stripe data (admin only).
     * Use with caution - this removes all simulated Payment Intents and Customers.
     *
     * @return Success message
     */
    @DeleteMapping("/clear")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiMessage> clearSimulatedData() {
        log.warn("Admin clearing all simulated Stripe data");

        stripeService.clearSimulatedData();
        return ResponseEntity.ok(ApiMessage.of("All simulated Stripe data cleared"));
    }

    /**
     * Simulate a refund (admin only).
     *
     * @param paymentIntentId Payment Intent ID
     * @param amount Amount to refund (optional, null for full refund)
     * @param reason Refund reason (optional)
     * @return Updated Payment Intent
     */
    @PostMapping("/refund/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentIntentResponse> refundPaymentIntent(
            @PathVariable("id") String paymentIntentId,
            @RequestParam(required = false) BigDecimal amount,
            @RequestParam(required = false) String reason) {

        log.info("Admin refunding Payment Intent: {}, amount: {}", paymentIntentId, amount);

        PaymentIntentResponse paymentIntent = stripeService.refundPaymentIntent(paymentIntentId, amount, reason);
        return ResponseEntity.ok(paymentIntent);
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
