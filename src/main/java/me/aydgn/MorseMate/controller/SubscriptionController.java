package me.aydgn.MorseMate.controller;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import me.aydgn.MorseMate.entity.User;
import me.aydgn.MorseMate.model.subscription.SubscriptionPlan;
import me.aydgn.MorseMate.service.StripeService;
import me.aydgn.MorseMate.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final StripeService stripeService;
    private final UserService userService;

    public record CreateCheckoutRequest(String planId) {}

    @PostMapping("/create-checkout-session")
    public ResponseEntity<Map<String, String>> createCheckoutSession(@RequestBody CreateCheckoutRequest request, @AuthenticationPrincipal Principal principal) throws StripeException {
        SubscriptionPlan plan = SubscriptionPlan.findById(request.planId());
        if (plan == null) {
            return ResponseEntity.badRequest().build();
        }

        User user = userService.getUserByUsername(principal.getName());

        String successUrl = "http://localhost:8080/payment/success?session_id={CHECKOUT_SESSION_ID}";
        String cancelUrl = "http://localhost:8080/payment/cancel";

        Session session = stripeService.createSubscriptionCheckoutSession(plan, user.getId(), successUrl, cancelUrl);

        return ResponseEntity.ok(Collections.singletonMap("url", session.getUrl()));
    }
}
