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

    @PostMapping("/create-subscription")
    public ResponseEntity<String> createSubscription(@RequestBody @Valid CreateSubscriptionRequestDto request) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body("This endpoint is not yet implemented.");
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("User not authenticated");
        }
        return Long.parseLong(authentication.getName());
    }
}