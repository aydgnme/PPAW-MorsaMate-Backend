package me.aydgn.MorseMate.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

/**
 * Entity for storing encrypted payment card information.
 * Sensitive fields are encrypted at application level using AES-GCM.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payment_cards",
       indexes = {
           @Index(name = "idx_payment_cards_user", columnList = "user_id"),
           @Index(name = "idx_payment_cards_fingerprint", columnList = "fingerprint", unique = true)
       })
@SQLDelete(sql = "UPDATE payment_cards SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class PaymentCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "cardholder_name", nullable = false, length = 128)
    private String cardholderName;

    @Column(name = "card_brand", nullable = false, length = 32)
    private String cardBrand;

    @Column(name = "card_last4", nullable = false, length = 4)
    private String cardLast4;

    /**
     * Encrypted PAN token (AES-256-GCM).
     */
    @Column(name = "card_token", nullable = false, length = 512)
    private String cardToken;

    /**
     * Encrypted CVV / CVC token (AES-256-GCM).
     */
    @Column(name = "cvv_token", nullable = false, length = 512)
    private String cvvToken;

    @Column(name = "expiry_month", nullable = false)
    private Integer expiryMonth;

    @Column(name = "expiry_year", nullable = false)
    private Integer expiryYear;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault;

    /**
     * Deterministic fingerprint used to detect duplicate cards for same user.
     * (e.g. SHA-256 of normalized PAN + expiry)
     */
    @Column(name = "fingerprint", nullable = false, length = 128, unique = true)
    private String fingerprint;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}


