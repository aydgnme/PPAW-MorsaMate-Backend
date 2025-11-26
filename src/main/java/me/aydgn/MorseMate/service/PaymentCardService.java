package me.aydgn.MorseMate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.dto.PaymentCardDTO;
import me.aydgn.MorseMate.entity.PaymentCard;
import me.aydgn.MorseMate.entity.User;
import me.aydgn.MorseMate.exception.ResourceNotFoundException;
import me.aydgn.MorseMate.repository.PaymentCardRepository;
import me.aydgn.MorseMate.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentCardService {

    private final PaymentCardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardCipherService cipherService;

    @Transactional(readOnly = true)
    public List<PaymentCardDTO> listCards(Long userId) {
        List<PaymentCard> cards = cardRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
        return cards.stream().map(this::toDto).toList();
    }

    @Transactional
    public PaymentCardDTO addCard(Long userId,
                                  String cardholderName,
                                  String cardNumber,
                                  String cvv,
                                  Integer expiryMonth,
                                  Integer expiryYear,
                                  boolean makeDefault) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        String fingerprint = createFingerprint(cardNumber, expiryMonth, expiryYear);

        LocalDateTime now = LocalDateTime.now();
        PaymentCard card = PaymentCard.builder()
                .user(user)
                .cardholderName(cardholderName)
                .cardBrand(detectBrand(cardNumber))
                .cardLast4(cardNumber.substring(cardNumber.length() - 4))
                .cardToken(cipherService.encrypt(cardNumber))
                .cvvToken(cipherService.encrypt(cvv))
                .expiryMonth(expiryMonth)
                .expiryYear(expiryYear)
                .isDefault(makeDefault || !cardRepository.existsByUserIdAndIsDefaultTrue(userId))
                .fingerprint(fingerprint)
                .createdAt(now)
                .updatedAt(now)
                .build();

        if (card.getIsDefault()) {
            // Unset previous defaults
            cardRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId)
                    .forEach(c -> {
                        if (!c.getId().equals(card.getId()) && Boolean.TRUE.equals(c.getIsDefault())) {
                            c.setIsDefault(false);
                        }
                    });
        }

        PaymentCard saved = cardRepository.save(card);
        return toDto(saved);
    }

    @Transactional
    public void removeCard(Long userId, Long cardId) {
        PaymentCard card = cardRepository.findByIdAndUserId(cardId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));
        cardRepository.delete(card);
    }

    @Transactional
    public void setDefaultCard(Long userId, Long cardId) {
        PaymentCard card = cardRepository.findByIdAndUserId(cardId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));

        List<PaymentCard> cards = cardRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
        cards.forEach(c -> c.setIsDefault(c.getId().equals(cardId)));
    }

    @Transactional(readOnly = true)
    public PaymentCard getCardForCharge(Long userId, Long cardId) {
        if (cardId != null) {
            return cardRepository.findByIdAndUserId(cardId, userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Card not found"));
        }

        return cardRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No saved card for this user"));
    }

    private PaymentCardDTO toDto(PaymentCard card) {
        return PaymentCardDTO.builder()
                .id(card.getId())
                .cardholderName(card.getCardholderName())
                .cardBrand(card.getCardBrand())
                .cardLast4(card.getCardLast4())
                .expiryMonth(card.getExpiryMonth())
                .expiryYear(card.getExpiryYear())
                .isDefault(card.getIsDefault())
                .createdAt(card.getCreatedAt())
                .build();
    }

    private String detectBrand(String cardNumber) {
        if (cardNumber.startsWith("4")) return "VISA";
        if (cardNumber.startsWith("5")) return "MASTERCARD";
        if (cardNumber.startsWith("3")) return "AMEX";
        return "UNKNOWN";
    }

    private String createFingerprint(String cardNumber, Integer month, Integer year) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String normalized = cardNumber.replaceAll("\\s+", "") + "|" + month + "|" + year;
            byte[] hash = digest.digest(normalized.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Could not generate card fingerprint", e);
        }
    }
}


