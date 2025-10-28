package me.aydgn.MorseMate.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "gem_transactions",
        indexes = @Index(name = "idx_gem_transactions_user", columnList = "user_id"))
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GemTransaction extends BaseEntity { // has created_at

    // id SERIAL PRIMARY KEY
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // user_id REFERENCES users(id) ON DELETE CASCADE
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "gem_transactions_user_id_fkey"))
    private User user;

    // amount INTEGER NOT NULL (pozitif/negatif olabilir)
    @Column(name = "amount", nullable = false)
    private Integer amount;

    // transaction_type VARCHAR(20) CHECK ('earn','spend','purchase','refund','bonus')
    public enum Type { EARN, SPEND, PURCHASE, REFUND, BONUS }

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", length = 20, nullable = false)
    private Type transactionType;

    // source VARCHAR(100)
    @Column(name = "source", length = 100)
    private String source;

    // description TEXT
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}