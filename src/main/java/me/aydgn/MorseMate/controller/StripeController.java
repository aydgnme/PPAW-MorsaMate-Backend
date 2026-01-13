package me.aydgn.MorseMate.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.entity.User;
import me.aydgn.MorseMate.service.StripeService;
import me.aydgn.MorseMate.service.SubscriptionService;
import me.aydgn.MorseMate.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stripe")
@RequiredArgsConstructor
@Slf4j
public class StripeController {

    private final StripeService stripeService;
    private final SubscriptionService subscriptionService;
    private final UserService userService;
    private static final Gson gson = new Gson();

    public record SimulateCheckoutRequest(String sessionId) {
    }

    /**
     * Simulates the completion of a checkout session.
     * This endpoint should be called by the frontend after a "successful" simulated
     * checkout.
     * It manually triggers the webhook logic.
     */
    @PostMapping("/simulate-checkout-completion")
    public ResponseEntity<String> simulateCheckoutCompletion(@RequestBody SimulateCheckoutRequest request) {
        log.info("Simulating checkout completion for session ID: {}", request.sessionId());

        com.stripe.model.checkout.Session session = StripeService.simulatedSessions.get(request.sessionId());
        if (session == null) {
            return ResponseEntity.badRequest().body("Simulated session not found.");
        }

        try {
            // Correctly construct a simulated Event object that matches what the service
            // expects
            String sessionJson = gson.toJson(session);
            JsonObject eventData = new JsonObject();
            eventData.add("object", gson.fromJson(sessionJson, JsonObject.class));

            JsonObject eventJson = new JsonObject();
            eventJson.addProperty("id", "evt_sim_" + UUID.randomUUID().toString());
            eventJson.addProperty("type", "checkout.session.completed");
            eventJson.add("data", eventData);

            Event event = gson.fromJson(eventJson, Event.class);

            subscriptionService.handleCheckoutSessionCompleted(event);
            return ResponseEntity.ok("Subscription activated successfully (simulated).");
        } catch (Exception e) {
            log.error("Error during simulated checkout completion", e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    /**
     * Creates a simulated Stripe Customer Portal session.
     */
    @PostMapping("/create-portal-session")
    public ResponseEntity<Map<String, String>> createPortalSession() throws StripeException {
        User user = userService.getCurrentAuthenticatedUser();

        if (user.getStripeCustomerId() == null) {
            stripeService.createCustomer(user);
        }

        String returnUrl = "http://localhost:8080/profile"; // URL to return to after portal
        com.stripe.model.billingportal.Session portalSession = stripeService
                .createCustomerPortalSession(user.getStripeCustomerId(), returnUrl);

        return ResponseEntity.ok(Collections.singletonMap("url", portalSession.getUrl()));
    }
}