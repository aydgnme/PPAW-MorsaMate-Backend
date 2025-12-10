package me.aydgn.MorseMate.controller;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Subscription;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.dto.request.CreatePaymentIntentRequest;
import me.aydgn.MorseMate.dto.request.CreateSubscriptionRequestDto;
import me.aydgn.MorseMate.entity.User;
import me.aydgn.MorseMate.service.AuthService;
import me.aydgn.MorseMate.service.StripeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/${api.version}/stripe")
@RequiredArgsConstructor
@Slf4j
public class StripeController {

    private final StripeService stripeService;
    private final AuthService authService;

    @PostMapping("/create-payment-intent")
    public ResponseEntity<String> createPaymentIntent(
            @Valid @RequestBody CreatePaymentIntentRequest request) {

        Long userId = getCurrentUserId();
        User user = authService.getCurrentUser(userId);

        if (user.getStripeCustomerId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User does not have a Stripe customer ID.");
        }

        try {
            // Convert amount to cents
            Long amountInCents = request.getAmount().multiply(new BigDecimal("100")).longValue();

            PaymentIntent paymentIntent = stripeService.createPaymentIntent(
                    amountInCents,
                    request.getCurrency(),
                    user.getStripeCustomerId()
            );
            return ResponseEntity.ok(paymentIntent.getClientSecret());
        } catch (StripeException e) {
            log.error("Error creating PaymentIntent for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/create-subscription")
    public ResponseEntity<String> createSubscription(@RequestBody @Valid CreateSubscriptionRequestDto request) {
        Long userId = getCurrentUserId();
        User user = authService.getCurrentUser(userId);

        if (user.getStripeCustomerId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User does not have a Stripe customer ID.");
        }

        try {
            Subscription subscription = stripeService.createSubscription(user.getStripeCustomerId(), request.getPriceId());
            // For subscriptions with `payment_behavior=default_incomplete`, the client secret is on the latest invoice's payment intent
            String clientSecret = subscription.getLatestInvoiceObject().getPaymentIntentObject().getClientSecret();
            return ResponseEntity.ok(clientSecret);
        } catch (StripeException e) {
            log.error("Error creating subscription for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("User not authenticated");
        }
        return Long.parseLong(authentication.getName());
    }
}