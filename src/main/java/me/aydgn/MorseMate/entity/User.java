package me.aydgn.MorseMate.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.Check;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Check(constraints = "hearts >= 0 AND hearts <= max_hearts AND level >= 1")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {
    // SQL: id SERIAL PRIMARY KEY
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // SQL: username VARCHAR(50) UNIQUE NOT NULL
    @NotBlank
    @Size(min = 3, max = 50)
    @Column(name = "username", nullable = false, length = 50, unique = true)
    private String username;

    // SQL: email VARCHAR(100) UNIQUE NOT NULL
    @NotBlank
    @Email
    @Size(max = 100)
    @Column(name = "email", nullable = false, length = 100, unique = true)
    private String email;

    // SQL: password_hash VARCHAR(255) NOT NULL
    @NotBlank
    @Size(max = 255)
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    // SQL: full_name VARCHAR(100)
    @Size(max = 100)
    @Column(name = "full_name", length = 100)
    private String fullName;

    // SQL: created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    // (DB default verecek; entity tarafında nullable tutuyoruz)
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // SQL: last_login TIMESTAMP
    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    // SQL: level INTEGER DEFAULT 1
    @Column(name = "level", nullable = false)
    @Builder.Default
    private Integer level = 1;

    // SQL: total_points INTEGER DEFAULT 0
    @Column(name = "total_points", nullable = false)
    @Builder.Default
    private Integer totalPoints = 0;

    // SQL: current_streak INTEGER DEFAULT 0
    @Column(name = "current_streak", nullable = false)
    @Builder.Default
    private Integer currentStreak = 0;

    // SQL: longest_streak INTEGER DEFAULT 0
    @Column(name = "longest_streak", nullable = false)
    @Builder.Default
    private Integer longestStreak = 0;

    // SQL: hearts INTEGER DEFAULT 5
    @Column(name = "hearts", nullable = false)
    @Builder.Default
    private Integer hearts = 5;

    // SQL: max_hearts INTEGER DEFAULT 5
    @Column(name = "max_hearts", nullable = false)
    @Builder.Default
    private Integer maxHearts = 5;

    // SQL: last_heart_refill TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    @Column(name = "last_heart_refill")
    private LocalDateTime lastHeartRefill;

    // SQL: profile_picture_url VARCHAR(255)
    @Size(max = 255)
    @Column(name = "profile_picture_url", length = 255)
    private String profilePictureUrl;

    // SQL: is_active BOOLEAN DEFAULT TRUE
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // SQL: email_verified BOOLEAN DEFAULT FALSE
    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    /* --------- Lifecycle hooks: DB varsayılanlarını ve normalizasyonu destekle --------- */
    @PrePersist
    private void prePersist() {
        // DB default’ları varsa null bırakmak sorun değil, fakat uygulama tarafında da doldurmak istersen:
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.lastHeartRefill == null) this.lastHeartRefill = LocalDateTime.now();

        normalize();
    }

    @PreUpdate
    private void preUpdate() {
        normalize();
    }

    private void normalize() {
        if (this.email != null) this.email = this.email.trim();
        if (this.username != null) this.username = this.username.trim();
        if (this.hearts != null && this.maxHearts != null && this.hearts > this.maxHearts) {
            this.hearts = this.maxHearts;
        }
        if (this.level == null) this.level = 1;
        if (this.totalPoints == null) this.totalPoints = 0;
    }

    /* ------------------ Domain methods (opsiyonel) ------------------ */

    public void addPoints(int points) {
        if (points <= 0) return;
        this.totalPoints += points;
        updateLevel();
    }

    public void updateLevel() {
        // SQL tarafındaki trigger/generate yoksa app-level hesap:
        this.level = (this.totalPoints / 100) + 1;
    }

    public void incrementStreak() {
        this.currentStreak++;
        if (this.currentStreak > this.longestStreak) {
            this.longestStreak = this.currentStreak;
        }
    }

    public void resetStreak() {
        this.currentStreak = 0;
    }

    public boolean canUseHeart() {
        return this.hearts != null && this.hearts > 0;
    }

    public boolean useHeart() {
        if (canUseHeart()) {
            this.hearts--;
            return true;
        }
        return false;
    }

    public void refillHearts() {
        this.hearts = this.maxHearts;
        this.lastHeartRefill = LocalDateTime.now();
    }

    public boolean needsHeartRefill() {
        if (this.hearts != null && this.maxHearts != null && this.hearts >= this.maxHearts) return false;
        if (this.lastHeartRefill == null) return true;
        return this.lastHeartRefill.plusHours(1).isBefore(LocalDateTime.now());
    }
}