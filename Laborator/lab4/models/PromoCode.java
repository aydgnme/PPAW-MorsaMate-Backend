package me.aydgn.MorseMate.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "promo_codes",
        uniqueConstraints = @UniqueConstraint(name = "uk_promo_codes_code", columnNames = "code"))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class PromoCode extends BaseEntity { // has created_at

    // id SERIAL PRIMARY KEY
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // code VARCHAR(50) UNIQUE NOT NULL
    @NotBlank
    @Size(max = 50)
    @Column(name = "code", nullable = false, length = 50, unique = true)
    private String code;

    // discount_type VARCHAR(20) CHECK ('percentage','fixed')
    public enum DiscountType { PERCENTAGE, FIXED }

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", length = 20, nullable = false)
    private DiscountType discountType;

    // discount_value DECIMAL(10,2) NOT NULL
    @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
    private java.math.BigDecimal discountValue;

    // max_uses INTEGER
    @Column(name = "max_uses")
    private Integer maxUses;

    // current_uses INTEGER DEFAULT 0
    @Column(name = "current_uses", nullable = false)
    @Builder.Default
    private Integer currentUses = 0;

    // valid_from TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    @Column(name = "valid_from")
    private LocalDateTime validFrom;

    // valid_until TIMESTAMP
    @Column(name = "valid_until")
    private LocalDateTime validUntil;

    // applicable_plans JSONB -- array of plan IDs
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "applicable_plans", columnDefinition = "jsonb")
    private List<Long> applicablePlans;

    // is_active BOOLEAN DEFAULT TRUE
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @PrePersist
    private void prePersist() {
        if (this.validFrom == null) this.validFrom = LocalDateTime.now();
    }
}