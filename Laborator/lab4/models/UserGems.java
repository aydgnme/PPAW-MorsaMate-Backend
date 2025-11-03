package me.aydgn.MorseMate.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_gems",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_gems_user", columnNames = "user_id"))
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserGems {

    // id SERIAL PRIMARY KEY
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // user_id UNIQUE REFERENCES users(id) ON DELETE CASCADE
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "user_gems_user_id_fkey"))
    private User user;

    // balance INTEGER DEFAULT 0 CHECK (>=0)
    @Min(0)
    @Column(name = "balance", nullable = false)
    @Builder.Default
    private Integer balance = 0;

    // total_earned INTEGER DEFAULT 0
    @Min(0)
    @Column(name = "total_earned", nullable = false)
    @Builder.Default
    private Integer totalEarned = 0;

    // total_spent INTEGER DEFAULT 0
    @Min(0)
    @Column(name = "total_spent", nullable = false)
    @Builder.Default
    private Integer totalSpent = 0;

    // last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @PrePersist @PreUpdate
    private void touch() {
        this.lastUpdated = LocalDateTime.now();
        if (balance < 0) balance = 0; // DB CHECK’iyle uyumlu güvenlik
    }

    /* helpers */
    public void earn(int amount) {
        if (amount <= 0) return;
        balance += amount;
        totalEarned += amount;
    }

    public boolean spend(int amount) {
        if (amount <= 0 || balance < amount) return false;
        balance -= amount;
        totalSpent += amount;
        return true;
    }
}