package me.aydgn.MorseMate.service;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.model.PaymentMethodCollection;
import com.stripe.model.Subscription;
import com.stripe.net.RequestOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.dto.request.CreatePaymentIntentRequest;
import me.aydgn.MorseMate.dto.response.PaymentIntentResponse;
import me.aydgn.MorseMate.dto.response.StripeCustomerResponse;
import me.aydgn.MorseMate.entity.User;
import me.aydgn.MorseMate.model.subscription.SubscriptionPlan;
import me.aydgn.MorseMate.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A service to SIMULATE Stripe API interactions.
 * This service does NOT make real calls to Stripe. It uses in-memory maps
 * to simulate the behavior of creating customers and checkout sessions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StripeService {

    private final UserRepository userRepository;

    // In-memory storage for simulated Stripe objects
    public static final Map<String, User> simulatedCustomers = new ConcurrentHashMap<>();
    public static final Map<String, com.stripe.model.checkout.Session> simulatedSessions = new ConcurrentHashMap<>();
    public static final Map<String, com.stripe.model.Subscription> simulatedSubscriptions = new ConcurrentHashMap<>();
    public static final Map<String, PaymentIntentResponse> simulatedPaymentIntents = new ConcurrentHashMap<>();


    /**
     * Simulates creating a Stripe customer.
     */
    public Customer createCustomer(User user) throws StripeException {
        if (user.getStripeCustomerId() != null) {
            log.info("Retrieving existing Stripe customer for user ID: {}", user.getId());
            Customer existing = Customer.retrieve(user.getStripeCustomerId());
            simulatedCustomers.put(user.getStripeCustomerId(), user);
            return existing;
        }

        log.info("Creating new Stripe customer for user ID: {}", user.getId());
        Map<String, Object> params = new HashMap<>();
        params.put("email", user.getEmail());
        Customer created = Customer.create(params);

        user.setStripeCustomerId(created.getId());
        userRepository.save(user);
        simulatedCustomers.put(created.getId(), user);

        log.info("Successfully created Stripe customer with ID: {}", created.getId());
        return created;
    }

    /**
     * Simulates creating a Stripe Checkout Session for a subscription plan.
     */
    public com.stripe.model.checkout.Session createSubscriptionCheckoutSession(SubscriptionPlan plan, Long userId, String successUrl, String cancelUrl) throws StripeException {
        log.info("Creating SIMULATED Stripe Checkout Session for plan: {} and user: {}", plan.getName(), userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found for simulation"));

        if (user.getStripeCustomerId() == null) {
            createCustomer(user);
        }

        String sessionId = "cs_test_sim_" + UUID.randomUUID().toString();
        String subscriptionId = "sub_sim_" + UUID.randomUUID().toString();

        // Create a simulated Subscription object
        Subscription subscription = new Subscription();
        subscription.setId(subscriptionId);
        subscription.setCustomer(user.getStripeCustomerId());
        subscription.setStatus("active");


        // Create a simulated Price object within the subscription
        com.stripe.model.Price price = new com.stripe.model.Price();
        price.setId(plan.getStripePriceId());
        price.setLookupKey(plan.getId()); // Using our local ID as lookup_key for mapping
        
        com.stripe.model.SubscriptionItem item = new com.stripe.model.SubscriptionItem();
        item.setPrice(price);
        subscription.setItems(new com.stripe.model.SubscriptionItemCollection());
        subscription.getItems().setData(List.of(item));

        simulatedSubscriptions.put(subscriptionId, subscription);

        // Create a simulated Checkout Session
        com.stripe.model.checkout.Session session = new com.stripe.model.checkout.Session();
        session.setId(sessionId);
        session.setMode("subscription");
        session.setCustomer(user.getStripeCustomerId());
        session.setSubscription(subscriptionId);
        session.setUrl(successUrl.replace("{CHECKOUT_SESSION_ID}", sessionId));
        
        Map<String, String> metadata = new HashMap<>();
        metadata.put("user_id", userId.toString());
        session.setMetadata(metadata);

        simulatedSessions.put(sessionId, session);

        log.info("Successfully created simulated Checkout Session with ID: {}", sessionId);
        return session;
    }

    /**
     * Simulates creating a Stripe Customer Portal session.
     */
    public com.stripe.model.billingportal.Session createCustomerPortalSession(String customerId, String returnUrl) throws StripeException {
        log.info("Creating SIMULATED Stripe Customer Portal Session for customer: {}", customerId);
        
        String portalSessionId = "bps_sim_" + UUID.randomUUID().toString();
        com.stripe.model.billingportal.Session portalSession = new com.stripe.model.billingportal.Session();
        portalSession.setId(portalSessionId);
        portalSession.setCustomer(customerId);
        portalSession.setReturnUrl(returnUrl);
        portalSession.setUrl(returnUrl + "?simulated_portal_session=" + portalSessionId);

        log.info("Successfully created simulated Customer Portal Session with ID: {}", portalSessionId);
        return portalSession;
    }

    /**
     * Simulates refunding a payment.
     */
    public com.stripe.model.Refund refund(String paymentIntentId, Long amount) {
        log.info("Simulating refund for payment intent ID: {} with amount: {}", paymentIntentId, amount);

        com.stripe.model.Refund refund = new com.stripe.model.Refund();
        refund.setId("re_sim_" + UUID.randomUUID().toString());
        refund.setAmount(amount);
        refund.setPaymentIntent(paymentIntentId);
        refund.setStatus("succeeded");

        return refund;
    }

    /**
     * Simulated createPaymentIntent using static Stripe API (for tests) and a local cache for controller flows.
     */
    public PaymentIntent createPaymentIntent(long amount, String currency, String customerId) throws StripeException {
        Map<String, Object> params = new HashMap<>();
        params.put("amount", amount);
        params.put("currency", currency);
        params.put("customer", customerId);
        return PaymentIntent.create(params, RequestOptions.getDefault());
    }

    /**
     * Simulated createPaymentIntent for REST flows (returns DTO and caches state).
     */
    public PaymentIntentResponse createPaymentIntent(Long userId, CreatePaymentIntentRequest request) {
        String id = "pi_sim_" + UUID.randomUUID();
        long amount = request.getAmount().multiply(java.math.BigDecimal.valueOf(100)).longValue();

        PaymentIntentResponse response = PaymentIntentResponse.builder()
                .id(id)
                .clientSecret(id + "_secret")
                .amount(amount)
                .amountDecimal(request.getAmount())
                .currency(request.getCurrency())
                .status(request.getSimulateFailure() ? "canceled" : "requires_confirmation")
                .paymentMethodTypes(new String[]{"card"})
                .metadata(Map.of("user_id", String.valueOf(userId), "simulated", "true"))
                .simulated(true)
                .created(Instant.now().getEpochSecond())
                .build();

        simulatedPaymentIntents.put(id, response);
        return response;
    }

    public PaymentIntentResponse confirmPaymentIntent(String paymentIntentId) {
        PaymentIntentResponse response = simulatedPaymentIntents.get(paymentIntentId);
        if (response == null) {
            return null;
        }
        PaymentIntentResponse updated = PaymentIntentResponse.builder()
                .id(response.getId())
                .clientSecret(response.getClientSecret())
                .amount(response.getAmount())
                .amountDecimal(response.getAmountDecimal())
                .currency(response.getCurrency())
                .status("succeeded")
                .paymentMethodTypes(response.getPaymentMethodTypes())
                .metadata(response.getMetadata())
                .simulated(true)
                .created(response.getCreated())
                .build();
        simulatedPaymentIntents.put(paymentIntentId, updated);
        return updated;
    }

    public PaymentIntentResponse cancelPaymentIntent(String paymentIntentId) {
        PaymentIntentResponse response = simulatedPaymentIntents.get(paymentIntentId);
        if (response == null) {
            return null;
        }
        PaymentIntentResponse updated = PaymentIntentResponse.builder()
                .id(response.getId())
                .clientSecret(response.getClientSecret())
                .amount(response.getAmount())
                .amountDecimal(response.getAmountDecimal())
                .currency(response.getCurrency())
                .status("canceled")
                .paymentMethodTypes(response.getPaymentMethodTypes())
                .metadata(response.getMetadata())
                .simulated(true)
                .created(response.getCreated())
                .build();
        simulatedPaymentIntents.put(paymentIntentId, updated);
        return updated;
    }

    public PaymentIntentResponse retrievePaymentIntent(String paymentIntentId) {
        return simulatedPaymentIntents.get(paymentIntentId);
    }

    public StripeCustomerResponse getCustomerByUserId(Long userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    if (user.getStripeCustomerId() == null) {
                        return null;
                    }
                    return StripeCustomerResponse.builder()
                            .id(user.getStripeCustomerId())
                            .email(user.getEmail())
                            .name(user.getUsername())
                            .userId(userId)
                            .simulated(true)
                            .created(Instant.now().getEpochSecond())
                            .build();
                })
                .orElse(null);
    }

    public Map<String, Object> simulateWebhookEvent(String eventType, String paymentIntentId) {
        Map<String, Object> event = new HashMap<>();
        event.put("id", "evt_sim_" + UUID.randomUUID());
        event.put("type", eventType);
        event.put("paymentIntentId", paymentIntentId);
        return event;
    }

    public Map<String, Object> getSimulationStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPaymentIntents", simulatedPaymentIntents.size());
        stats.put("totalCustomers", simulatedCustomers.size());
        stats.put("totalSubscriptions", simulatedSubscriptions.size());
        return stats;
    }

    public PaymentIntentResponse refundPaymentIntent(String paymentIntentId, Object amount, Object reason) {
        PaymentIntentResponse response = simulatedPaymentIntents.get(paymentIntentId);
        if (response == null) {
            return null;
        }
        PaymentIntentResponse updated = PaymentIntentResponse.builder()
                .id(response.getId())
                .clientSecret(response.getClientSecret())
                .amount(response.getAmount())
                .amountDecimal(response.getAmountDecimal())
                .currency(response.getCurrency())
                .status("succeeded")
                .paymentMethodTypes(response.getPaymentMethodTypes())
                .metadata(response.getMetadata())
                .simulated(true)
                .created(response.getCreated())
                .build();
        simulatedPaymentIntents.put(paymentIntentId, updated);
        return updated;
    }

    public Subscription createSubscription(String customerId, String priceId) throws StripeException {
        Map<String, Object> params = new HashMap<>();
        params.put("customer", customerId);
        params.put("items", List.of(Map.of("price", priceId)));
        return Subscription.create(params, RequestOptions.getDefault());
    }

    public Subscription cancelSubscription(String subscriptionId) throws StripeException {
        Subscription subscription = Subscription.retrieve(subscriptionId);
        return subscription.cancel(new HashMap<>());
    }

    public PaymentMethodCollection listPaymentMethods(String customerId) throws StripeException {
        Map<String, Object> params = new HashMap<>();
        params.put("customer", customerId);
        params.put("type", "card");
        return PaymentMethod.list(params);
    }

    public PaymentMethod attachPaymentMethod(String customerId, String paymentMethodId) throws StripeException {
        PaymentMethod pm = PaymentMethod.retrieve(paymentMethodId);
        Map<String, Object> params = new HashMap<>();
        params.put("customer", customerId);
        return pm.attach(params);
    }
}
