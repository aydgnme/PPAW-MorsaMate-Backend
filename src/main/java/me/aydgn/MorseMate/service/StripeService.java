package me.aydgn.MorseMate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.dto.request.CreatePaymentIntentRequest;
import me.aydgn.MorseMate.dto.response.PaymentIntentResponse;
import me.aydgn.MorseMate.dto.response.StripeCustomerResponse;
import me.aydgn.MorseMate.entity.User;
import me.aydgn.MorseMate.exception.InvalidOperationException;
import me.aydgn.MorseMate.exception.ResourceNotFoundException;
import me.aydgn.MorseMate.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Simulated Stripe Service.
 *
 * SIMULATION MODE:
 * This service simulates Stripe API operations without actual Stripe integration.
 * All Stripe objects (PaymentIntent, Customer, etc.) are mocked for testing.
 * Real Stripe integration can be added later by replacing simulation logic
 * with actual Stripe API calls.
 *
 * Simulated Stripe Operations:
 * - Create Payment Intent
 * - Confirm Payment Intent
 * - Create Customer
 * - Retrieve Customer
 * - Cancel Payment Intent
 * - Webhook event simulation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StripeService {

    private final UserRepository userRepository;

    // In-memory storage for simulated Stripe objects (in real app, Stripe manages these)
    private final Map<String, PaymentIntentResponse> paymentIntents = new HashMap<>();
    private final Map<Long, StripeCustomerResponse> customersByUserId = new HashMap<>();
    private final Map<String, StripeCustomerResponse> customersById = new HashMap<>();

    /**
     * Create a simulated Payment Intent.
     *
     * @param userId User ID making the payment
     * @param request Payment Intent creation request
     * @return Simulated Payment Intent
     */
    public PaymentIntentResponse createPaymentIntent(Long userId, CreatePaymentIntentRequest request) {
        log.info("Creating simulated Payment Intent for user ID: {}, amount: {} {}",
                userId, request.getAmount(), request.getCurrency());

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Get or create Stripe customer
        StripeCustomerResponse customer = getOrCreateCustomer(user);

        // Generate simulated IDs
        String paymentIntentId = "pi_sim_" + UUID.randomUUID().toString().substring(0, 24);
        String clientSecret = paymentIntentId + "_secret_" + UUID.randomUUID().toString().substring(0, 16);

        // Convert amount to cents (Stripe uses smallest currency unit)
        Long amountInCents = request.getAmount().multiply(new BigDecimal("100")).longValue();

        // Determine initial status
        String status = request.getSimulateFailure() != null && request.getSimulateFailure()
                ? "requires_payment_method"  // Will fail when confirmed
                : "requires_confirmation";    // Ready to be confirmed

        // Build metadata
        Map<String, String> metadata = new HashMap<>();
        if (request.getMetadata() != null) {
            metadata.putAll(request.getMetadata());
        }
        metadata.put("user_id", userId.toString());
        metadata.put("customer_id", customer.getId());
        metadata.put("simulated", "true");
        if (request.getPlanId() != null) {
            metadata.put("plan_id", request.getPlanId().toString());
        }
        if (request.getPromoCode() != null) {
            metadata.put("promo_code", request.getPromoCode());
        }

        // Create Payment Intent response
        PaymentIntentResponse paymentIntent = PaymentIntentResponse.builder()
                .id(paymentIntentId)
                .clientSecret(clientSecret)
                .amount(amountInCents)
                .amountDecimal(request.getAmount())
                .currency(request.getCurrency().toLowerCase())
                .status(status)
                .paymentMethodTypes(request.getPaymentMethodTypes())
                .metadata(metadata)
                .simulated(true)
                .created(Instant.now().getEpochSecond())
                .build();

        // Store in memory (simulating Stripe's database)
        paymentIntents.put(paymentIntentId, paymentIntent);

        log.info("Simulated Payment Intent created: {}, status: {}", paymentIntentId, status);
        return paymentIntent;
    }

    /**
     * Confirm a simulated Payment Intent.
     *
     * @param paymentIntentId Payment Intent ID
     * @return Updated Payment Intent
     */
    public PaymentIntentResponse confirmPaymentIntent(String paymentIntentId) {
        log.info("Confirming simulated Payment Intent: {}", paymentIntentId);

        PaymentIntentResponse paymentIntent = paymentIntents.get(paymentIntentId);
        if (paymentIntent == null) {
            throw new ResourceNotFoundException("Payment Intent not found: " + paymentIntentId);
        }

        if ("succeeded".equals(paymentIntent.getStatus())) {
            throw new InvalidOperationException("Payment Intent already succeeded");
        }

        if ("canceled".equals(paymentIntent.getStatus())) {
            throw new InvalidOperationException("Payment Intent is canceled");
        }

        // Simulate payment processing
        // Check if this was marked to fail
        boolean shouldFail = paymentIntent.getMetadata().containsKey("simulate_failure")
                || "requires_payment_method".equals(paymentIntent.getStatus());

        if (shouldFail) {
            paymentIntent.setStatus("requires_payment_method");
            log.warn("Simulated Payment Intent failed: {}", paymentIntentId);
        } else {
            paymentIntent.setStatus("succeeded");
            log.info("Simulated Payment Intent succeeded: {}", paymentIntentId);
        }

        paymentIntents.put(paymentIntentId, paymentIntent);
        return paymentIntent;
    }

    /**
     * Cancel a simulated Payment Intent.
     *
     * @param paymentIntentId Payment Intent ID
     * @return Updated Payment Intent
     */
    public PaymentIntentResponse cancelPaymentIntent(String paymentIntentId) {
        log.info("Canceling simulated Payment Intent: {}", paymentIntentId);

        PaymentIntentResponse paymentIntent = paymentIntents.get(paymentIntentId);
        if (paymentIntent == null) {
            throw new ResourceNotFoundException("Payment Intent not found: " + paymentIntentId);
        }

        if ("succeeded".equals(paymentIntent.getStatus())) {
            throw new InvalidOperationException("Cannot cancel succeeded Payment Intent");
        }

        if ("canceled".equals(paymentIntent.getStatus())) {
            throw new InvalidOperationException("Payment Intent already canceled");
        }

        paymentIntent.setStatus("canceled");
        paymentIntents.put(paymentIntentId, paymentIntent);

        log.info("Simulated Payment Intent canceled: {}", paymentIntentId);
        return paymentIntent;
    }

    /**
     * Retrieve a simulated Payment Intent.
     *
     * @param paymentIntentId Payment Intent ID
     * @return Payment Intent
     */
    public PaymentIntentResponse retrievePaymentIntent(String paymentIntentId) {
        PaymentIntentResponse paymentIntent = paymentIntents.get(paymentIntentId);
        if (paymentIntent == null) {
            throw new ResourceNotFoundException("Payment Intent not found: " + paymentIntentId);
        }
        return paymentIntent;
    }

    /**
     * Get or create a simulated Stripe Customer for a user.
     *
     * @param user User entity
     * @return Simulated Stripe Customer
     */
    public StripeCustomerResponse getOrCreateCustomer(User user) {
        // Check if customer already exists
        StripeCustomerResponse existingCustomer = customersByUserId.get(user.getId());
        if (existingCustomer != null) {
            log.debug("Returning existing simulated customer: {}", existingCustomer.getId());
            return existingCustomer;
        }

        // Create new simulated customer
        String customerId = "cus_sim_" + UUID.randomUUID().toString().substring(0, 24);

        StripeCustomerResponse customer = StripeCustomerResponse.builder()
                .id(customerId)
                .email(user.getEmail())
                .name(user.getFullName())
                .userId(user.getId())
                .simulated(true)
                .created(Instant.now().getEpochSecond())
                .build();

        // Store in memory
        customersByUserId.put(user.getId(), customer);
        customersById.put(customerId, customer);

        log.info("Created simulated Stripe customer: {} for user ID: {}", customerId, user.getId());
        return customer;
    }

    /**
     * Retrieve a simulated Stripe Customer by ID.
     *
     * @param customerId Customer ID
     * @return Simulated Stripe Customer
     */
    public StripeCustomerResponse retrieveCustomer(String customerId) {
        StripeCustomerResponse customer = customersById.get(customerId);
        if (customer == null) {
            throw new ResourceNotFoundException("Customer not found: " + customerId);
        }
        return customer;
    }

    /**
     * Get customer by user ID.
     *
     * @param userId User ID
     * @return Simulated Stripe Customer or null
     */
    public StripeCustomerResponse getCustomerByUserId(Long userId) {
        return customersByUserId.get(userId);
    }

    /**
     * Simulate a refund for a Payment Intent.
     *
     * @param paymentIntentId Payment Intent ID
     * @param amount Amount to refund (null for full refund)
     * @param reason Refund reason
     * @return Updated Payment Intent
     */
    public PaymentIntentResponse refundPaymentIntent(String paymentIntentId, BigDecimal amount, String reason) {
        log.info("Simulating refund for Payment Intent: {}, amount: {}", paymentIntentId, amount);

        PaymentIntentResponse paymentIntent = paymentIntents.get(paymentIntentId);
        if (paymentIntent == null) {
            throw new ResourceNotFoundException("Payment Intent not found: " + paymentIntentId);
        }

        if (!"succeeded".equals(paymentIntent.getStatus())) {
            throw new InvalidOperationException("Can only refund succeeded Payment Intents");
        }

        // Add refund metadata
        Map<String, String> metadata = paymentIntent.getMetadata();
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        metadata.put("refunded", "true");
        metadata.put("refund_amount", amount != null ? amount.toString() : paymentIntent.getAmountDecimal().toString());
        metadata.put("refund_reason", reason != null ? reason : "requested_by_customer");
        metadata.put("refund_timestamp", Instant.now().toString());
        paymentIntent.setMetadata(metadata);

        // Update status (in real Stripe, there's no "refunded" status, but we'll add it for clarity)
        paymentIntent.getMetadata().put("status_after_refund", "refunded");

        paymentIntents.put(paymentIntentId, paymentIntent);

        log.info("Simulated refund completed for Payment Intent: {}", paymentIntentId);
        return paymentIntent;
    }

    /**
     * Simulate a webhook event.
     * In real Stripe, webhooks are sent by Stripe to your endpoint.
     * This method simulates what would happen when a webhook is received.
     *
     * @param eventType Event type (e.g., payment_intent.succeeded)
     * @param paymentIntentId Payment Intent ID
     * @return Event data
     */
    public Map<String, Object> simulateWebhookEvent(String eventType, String paymentIntentId) {
        log.info("Simulating webhook event: {} for Payment Intent: {}", eventType, paymentIntentId);

        PaymentIntentResponse paymentIntent = paymentIntents.get(paymentIntentId);
        if (paymentIntent == null) {
            throw new ResourceNotFoundException("Payment Intent not found: " + paymentIntentId);
        }

        Map<String, Object> event = new HashMap<>();
        event.put("id", "evt_sim_" + UUID.randomUUID().toString().substring(0, 24));
        event.put("type", eventType);
        event.put("created", Instant.now().getEpochSecond());
        event.put("livemode", false);
        event.put("simulated", true);

        Map<String, Object> data = new HashMap<>();
        data.put("object", paymentIntent);
        event.put("data", data);

        log.info("Simulated webhook event created: {}", event.get("id"));
        return event;
    }

    /**
     * Clear all simulated data (for testing purposes).
     */
    public void clearSimulatedData() {
        paymentIntents.clear();
        customersByUserId.clear();
        customersById.clear();
        log.info("Cleared all simulated Stripe data");
    }

    /**
     * Get statistics about simulated Stripe objects.
     *
     * @return Statistics map
     */
    public Map<String, Object> getSimulationStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPaymentIntents", paymentIntents.size());
        stats.put("totalCustomers", customersById.size());
        stats.put("succeededPayments", paymentIntents.values().stream()
                .filter(pi -> "succeeded".equals(pi.getStatus()))
                .count());
        stats.put("failedPayments", paymentIntents.values().stream()
                .filter(pi -> "requires_payment_method".equals(pi.getStatus()))
                .count());
        stats.put("canceledPayments", paymentIntents.values().stream()
                .filter(pi -> "canceled".equals(pi.getStatus()))
                .count());
        return stats;
    }
}
