package me.aydgn.MorseMate.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(
        name = "payments",
        indexes = {
                @Index(name = "idx_payments_user", columnList = "user_id")
        }
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    // id SERIAL PRIMARY KEY
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // user_id REFERENCES users(id) ON DELETE CASCADE
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "payments_user_id_fkey")
    )
    private User user;

    // subscription_id REFERENCES user_subscriptions(id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "subscription_id",
            foreignKey = @ForeignKey(name = "payments_subscription_id_fkey")
    )
    private UserSubscription subscription;

    // amount DECIMAL(10,2) NOT NULL
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    // currency VARCHAR(3) DEFAULT 'USD'
    @Size(max = 3)
    @Column(name = "currency", length = 3)
    private String currency;

    // status VARCHAR(20) CHECK ('pending','completed','failed','refunded')
    public enum Status { PENDING, COMPLETED, FAILED, REFUNDED }

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private Status status;

    // payment_method VARCHAR(50)
    @Size(max = 50)
    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    // stripe_payment_id VARCHAR(100)
    @Size(max = 100)
    @Column(name = "stripe_payment_id", length = 100)
    private String stripePaymentId;

    // transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    // metadata JSONB
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    /* lifecycle */
    @PrePersist
    private void prePersist() {
        if (this.transactionDate == null) this.transactionDate = LocalDateTime.now();
        if (this.currency == null) this.currency = "USD";
        if (this.status == null) this.status = Status.PENDING;
    }
}