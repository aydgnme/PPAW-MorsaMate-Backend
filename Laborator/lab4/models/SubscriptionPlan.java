package me.aydgn.MorseMate.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.Map;

@Entity
@Table(name = "subscription_plans")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPlan extends BaseEntity {

    // id SERIAL PRIMARY KEY
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // name VARCHAR(50) NOT NULL
    @NotBlank
    @Size(max = 50)
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    // description TEXT
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // price DECIMAL(10,2) NOT NULL
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    // billing_period VARCHAR(20) CHECK ('monthly','yearly','lifetime')
    public enum BillingPeriod { MONTHLY, YEARLY, LIFETIME }

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_period", length = 20)
    private BillingPeriod billingPeriod;

    // features JSONB
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "features", columnDefinition = "jsonb")
    private Map<String, Object> features;

    // max_hearts INTEGER DEFAULT 5
    @Column(name = "max_hearts", nullable = false)
    @Builder.Default
    private Integer maxHearts = 5;

    // daily_practice_limit INTEGER
    @Column(name = "daily_practice_limit")
    private Integer dailyPracticeLimit;

    // is_active BOOLEAN DEFAULT TRUE
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}