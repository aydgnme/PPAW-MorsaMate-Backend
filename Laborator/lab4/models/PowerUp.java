package me.aydgn.MorseMate.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "power_ups")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class PowerUp extends BaseEntity { // has created_at

    // id SERIAL PRIMARY KEY
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // name VARCHAR(100) NOT NULL
    @NotBlank
    @Size(max = 100)
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    // description TEXT
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // type VARCHAR(50) CHECK ('xp_boost','heart_refill','streak_freeze','unlimited_hearts')
    public enum Type { XP_BOOST, HEART_REFILL, STREAK_FREEZE, UNLIMITED_HEARTS }

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 50, nullable = false)
    private Type type;

    // cost_gems INTEGER NOT NULL
    @Min(0)
    @Column(name = "cost_gems", nullable = false)
    private Integer costGems;

    // duration_hours INTEGER (NULL for instant)
    @Column(name = "duration_hours")
    private Integer durationHours;

    // icon VARCHAR(50)
    @Size(max = 50)
    @Column(name = "icon", length = 50)
    private String icon;

    // is_active BOOLEAN DEFAULT TRUE
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}