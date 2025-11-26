package me.aydgn.MorseMate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.dto.PaymentCardDTO;
import me.aydgn.MorseMate.service.PaymentCardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing user's saved payment cards.
 */
@RestController
@RequestMapping("/${api.version}/payment-cards")
@RequiredArgsConstructor
@Slf4j
public class PaymentCardController {

    private final PaymentCardService cardService;

    @GetMapping
    public ResponseEntity<List<PaymentCardDTO>> listCards() {
        Long userId = getCurrentUserId();
        List<PaymentCardDTO> cards = cardService.listCards(userId);
        return ResponseEntity.ok(cards);
    }

    @PostMapping
    public ResponseEntity<PaymentCardDTO> addCard(@RequestBody AddCardRequest request) {
        Long userId = getCurrentUserId();
        PaymentCardDTO card = cardService.addCard(
                userId,
                request.cardholderName(),
                request.cardNumber(),
                request.cvv(),
                request.expiryMonth(),
                request.expiryYear(),
                request.makeDefault()
        );
        return ResponseEntity.ok(card);
    }

    @DeleteMapping("/{cardId}")
    public ResponseEntity<Void> removeCard(@PathVariable Long cardId) {
        Long userId = getCurrentUserId();
        cardService.removeCard(userId, cardId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{cardId}/default")
    public ResponseEntity<Void> setDefaultCard(@PathVariable Long cardId) {
        Long userId = getCurrentUserId();
        cardService.setDefaultCard(userId, cardId);
        return ResponseEntity.noContent().build();
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }
        return Long.parseLong(authentication.getName());
    }

    /**
     * Simple request body record for adding a card.
     */
    public record AddCardRequest(
            String cardholderName,
            String cardNumber,
            String cvv,
            Integer expiryMonth,
            Integer expiryYear,
            boolean makeDefault
    ) {
    }
}


