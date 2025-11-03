package me.aydgn.MorseMate.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_power_ups")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPowerUp {

    // id SERIAL PRIMARY KEY
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // user_id REFERENCES users(id) ON DELETE CASCADE
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "user_power_ups_user_id_fkey"))
    private User user;

    // power_up_id REFERENCES power_ups(id)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "power_up_id", nullable = false,
            foreignKey = @ForeignKey(name = "user_power_ups_power_up_id_fkey"))
    private PowerUp powerUp;

    // purchased_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    @Column(name = "purchased_at")
    private LocalDateTime purchasedAt;

    // activated_at TIMESTAMP
    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    // expires_at TIMESTAMP
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    // is_active BOOLEAN DEFAULT FALSE
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = false;

    // is_used BOOLEAN DEFAULT FALSE
    @Column(name = "is_used", nullable = false)
    @Builder.Default
    private Boolean isUsed = false;

    @PrePersist
    private void prePersist() {
        if (this.purchasedAt == null) this.purchasedAt = LocalDateTime.now();
    }
}