package me.aydgn.MorseMate.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_subscriptions",
        indexes = {
                @Index(name = "idx_user_subscriptions_user", columnList = "user_id")
        }
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSubscription extends BaseEntity {

    // id SERIAL PRIMARY KEY
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // user_id UNIQUE, REFERENCES users(id)
    // Şemanda user bazında tek satır: @OneToOne en doğal eşleşme
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "user_subscriptions_user_id_key")
    )
    private User user;

    // plan_id REFERENCES subscription_plans(id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "plan_id",
            foreignKey = @ForeignKey(name = "user_subscriptions_plan_id_fkey")
    )
    private SubscriptionPlan plan;

    // status VARCHAR(20) CHECK ('active','cancelled','expired','paused') DEFAULT 'active'
    public enum Status { ACTIVE, CANCELLED, EXPIRED, PAUSED }

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private Status status = Status.ACTIVE;

    // start_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    @Column(name = "start_date")
    private LocalDateTime startDate;

    // end_date TIMESTAMP
    @Column(name = "end_date")
    private LocalDateTime endDate;

    // next_billing_date TIMESTAMP
    @Column(name = "next_billing_date")
    private LocalDateTime nextBillingDate;

    // auto_renew BOOLEAN DEFAULT TRUE
    @Column(name = "auto_renew", nullable = false)
    @Builder.Default
    private Boolean autoRenew = true;

    // stripe_subscription_id VARCHAR(100)
    @Column(name = "stripe_subscription_id", length = 100)
    private String stripeSubscriptionId;

    /* lifecycle */
    @PrePersist
    private void prePersist() {
        if (this.startDate == null) this.startDate = LocalDateTime.now();
    }
}