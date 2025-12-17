package me.aydgn.MorseMate.service;

import com.stripe.model.Event;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.entity.User;
import me.aydgn.MorseMate.entity.UserSubscription;
import me.aydgn.MorseMate.repository.UserRepository;
import me.aydgn.MorseMate.repository.UserSubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final UserRepository userRepository;
    private final UserSubscriptionRepository subscriptionRepository;
    private static final Gson gson = new Gson();

    @Transactional
    public void handleCheckoutSessionCompleted(Event event) {
        Session session = gson.fromJson(event.getData().getObject().toString(), Session.class);

        // This metadata must be set during checkout session creation to link the session to a user
        String userIdStr = Optional.ofNullable(session.getMetadata())
                .map(metadata -> metadata.get("user_id"))
                .orElse(null);

        if (userIdStr == null) {
            log.error("Webhook Error: user_id not found in checkout.session.completed metadata. Session ID: {}", session.getId());
            return;
        }

        Long userId = Long.parseLong(userIdStr);
        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            log.error("Webhook Error: User not found for user_id: {}. Session ID: {}", userId, session.getId());
            return;
        }

        // Retrieve the full subscription object from Stripe
        Subscription subscription;
        try {
            subscription = Subscription.retrieve(session.getSubscription());
        } catch (Exception e) {
            log.error("Webhook Error: Failed to retrieve subscription {} from Stripe.", session.getSubscription(), e);
            return;
        }

        // Check if we have already processed this subscription
        if (subscriptionRepository.findByStripeSubscriptionId(subscription.getId()).isPresent()) {
            log.warn("Webhook Warning: Received duplicate checkout.session.completed event for subscription ID: {}", subscription.getId());
            return;
        }
        
        String planId = subscription.getItems().getData().get(0).getPrice().getLookupKey();
        if (planId == null) {
             planId = subscription.getItems().getData().get(0).getPrice().getId();
        }

        UserSubscription userSubscription = UserSubscription.builder()
                .user(user)
                .stripeSubscriptionId(subscription.getId())
                .stripeCustomerId(subscription.getCustomer())
                .planId(Long.valueOf(planId))
                .status(UserSubscription.SubscriptionStatus.valueOf(subscription.getStatus().toUpperCase()))
                .build();

        subscriptionRepository.save(userSubscription);
        log.info("Successfully created new subscription for user ID: {}. Subscription ID: {}", userId, subscription.getId());
    }

    @Transactional
    public void handleSubscriptionUpdatedOrDeleted(Event event) {
        Subscription subscription = gson.fromJson(event.getData().getObject().toString(), Subscription.class);
        Optional<UserSubscription> existingSubOptional = subscriptionRepository.findByStripeSubscriptionId(subscription.getId());

        if (existingSubOptional.isEmpty()) {
            log.warn("Webhook Warning: Received subscription update/delete event for an unknown subscription ID: {}", subscription.getId());
            return;
        }

        UserSubscription existingSub = existingSubOptional.get();
        existingSub.setStatus(UserSubscription.SubscriptionStatus.valueOf(subscription.getStatus().toUpperCase()));


        subscriptionRepository.save(existingSub);
        log.info("Successfully updated subscription for user ID: {}. New status: {}", existingSub.getUser().getId(), existingSub.getStatus());
    }
}
