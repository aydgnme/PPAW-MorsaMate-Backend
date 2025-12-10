package me.aydgn.MorseMate.service;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.model.PaymentMethodCollection;
import com.stripe.model.Subscription;
import com.stripe.net.RequestOptions;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentMethodAttachParams;
import com.stripe.param.PaymentMethodListParams;
import com.stripe.param.SubscriptionCancelParams;
import com.stripe.param.SubscriptionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.entity.User;
import me.aydgn.MorseMate.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeService {

    private final UserRepository userRepository;

    /**
     * Creates a new Stripe customer for the given user.
     * If the user already has a Stripe customer ID, it retrieves and returns the existing customer.
     *
     * @param user The user to create a Stripe customer for.
     * @return The created or retrieved Stripe Customer object.
     * @throws StripeException if an error occurs while interacting with the Stripe API.
     */
    public Customer createCustomer(User user) throws StripeException {
        if (user.getStripeCustomerId() != null) {
            try {
                log.info("Retrieving existing Stripe customer for user ID: {}", user.getId());
                return Customer.retrieve(user.getStripeCustomerId());
            } catch (StripeException e) {
                log.error("Failed to retrieve Stripe customer with ID: {}. Will create a new one.", user.getStripeCustomerId(), e);
                // Fall through to create a new customer if retrieval fails
            }
        }

        log.info("Creating new Stripe customer for user ID: {}", user.getId());

        CustomerCreateParams params = CustomerCreateParams.builder()
                .setName(user.getFullName())
                .setEmail(user.getEmail())
                .putMetadata("app_user_id", user.getId().toString())
                .putMetadata("app_username", user.getUsername())
                .build();

        Customer customer = Customer.create(params);

        user.setStripeCustomerId(customer.getId());
        userRepository.save(user);

        log.info("Successfully created Stripe customer with ID: {} for user ID: {}", customer.getId(), user.getId());

        return customer;
    }

    /**
     * Creates a PaymentIntent for a one-time payment.
     *
     * @param amount   The amount to charge, in the smallest currency unit (e.g., cents).
     * @param currency The three-letter ISO currency code.
     * @param customerId The ID of the Stripe customer making the payment.
     * @return The created PaymentIntent.
     * @throws StripeException If an error occurs during the API call.
     */
    public PaymentIntent createPaymentIntent(Long amount, String currency, String customerId) throws StripeException {
        log.info("Creating PaymentIntent for customer: {}, amount: {}, currency: {}", customerId, amount, currency);

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amount)
                .setCurrency(currency)
                .setCustomer(customerId)
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build()
                )
                .build();

        // Use an idempotency key to prevent creating duplicate charges
        RequestOptions requestOptions = RequestOptions.builder()
                .setIdempotencyKey(UUID.randomUUID().toString())
                .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params, requestOptions);

        log.info("Successfully created PaymentIntent with ID: {}", paymentIntent.getId());

        return paymentIntent;
    }

    /**
     * Creates a new subscription for a customer.
     *
     * @param customerId The ID of the Stripe customer.
     * @param priceId    The ID of the price for the subscription plan.
     * @return The created Subscription.
     * @throws StripeException If an error occurs during the API call.
     */
    public Subscription createSubscription(String customerId, String priceId) throws StripeException {
        log.info("Creating subscription for customer: {} with price: {}", customerId, priceId);

        SubscriptionCreateParams params = SubscriptionCreateParams.builder()
                .setCustomer(customerId)
                .addItem(SubscriptionCreateParams.Item.builder().setPrice(priceId).build())
                .setPaymentBehavior(SubscriptionCreateParams.PaymentBehavior.DEFAULT_INCOMPLETE)
                .setExpand(java.util.Arrays.asList("latest_invoice.payment_intent"))
                .build();

        RequestOptions requestOptions = RequestOptions.builder()
                .setIdempotencyKey(UUID.randomUUID().toString())
                .build();

        Subscription subscription = Subscription.create(params, requestOptions);

        log.info("Successfully created subscription with ID: {}", subscription.getId());

        return subscription;
    }

    /**
     * Cancels a subscription at the end of the current billing period.
     *
     * @param subscriptionId The ID of the subscription to cancel.
     * @return The canceled Subscription.
     * @throws StripeException If an error occurs during the API call.
     */
    public Subscription cancelSubscription(String subscriptionId) throws StripeException {
        log.info("Canceling subscription with ID: {}", subscriptionId);

        Subscription subscription = Subscription.retrieve(subscriptionId);

        SubscriptionCancelParams params = SubscriptionCancelParams.builder().build();

        Subscription canceledSubscription = subscription.cancel(params);

        log.info("Successfully canceled subscription with ID: {}", canceledSubscription.getId());

        return canceledSubscription;
    }

    /**
     * Lists the payment methods attached to a customer.
     *
     * @param customerId The ID of the Stripe customer.
     * @return A collection of payment methods.
     * @throws StripeException If an error occurs during the API call.
     */
    public PaymentMethodCollection listPaymentMethods(String customerId) throws StripeException {
        log.info("Listing payment methods for customer: {}", customerId);

        PaymentMethodListParams params = PaymentMethodListParams.builder()
                .setCustomer(customerId)
                .setType(PaymentMethodListParams.Type.CARD)
                .build();

        PaymentMethodCollection paymentMethods = PaymentMethod.list(params);

        log.info("Found {} payment methods for customer: {}", paymentMethods.getData().size(), customerId);

        return paymentMethods;
    }

    /**
     * Attaches a payment method to a customer.
     *
     * @param customerId      The ID of the Stripe customer.
     * @param paymentMethodId The ID of the payment method to attach.
     * @return The attached PaymentMethod.
     * @throws StripeException If an error occurs during the API call.
     */
    public PaymentMethod attachPaymentMethod(String customerId, String paymentMethodId) throws StripeException {
        log.info("Attaching payment method: {} to customer: {}", paymentMethodId, customerId);

        PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);

        PaymentMethodAttachParams params = PaymentMethodAttachParams.builder()
                .setCustomer(customerId)
                .build();

        PaymentMethod attachedPaymentMethod = paymentMethod.attach(params);

        log.info("Successfully attached payment method: {} to customer: {}", attachedPaymentMethod.getId(), customerId);

        return attachedPaymentMethod;
    }
}
