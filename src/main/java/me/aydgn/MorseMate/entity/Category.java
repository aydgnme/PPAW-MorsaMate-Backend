package me.aydgn.MorseMate.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Category entity with soft delete support
 * When deleted, only deleted_at timestamp is set
 * Queries automatically filter out deleted records
 */
@Entity
@Table(name = "categories")
@SQLDelete(sql = "UPDATE categories SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category extends BaseEntity {

    // SQL: id SERIAL PRIMARY KEY
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // SQL: name VARCHAR(100) NOT NULL
    @NotBlank
    @Size(max = 100)
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    // SQL: description TEXT
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // SQL: display_order INTEGER
    @Column(name = "display_order")
    private Integer displayOrder;

    // SQL: icon_url VARCHAR(255)
    @Size(max = 255)
    @Column(name = "icon_url", length = 255)
    private String iconUrl;

    // SQL: is_active BOOLEAN DEFAULT TRUE
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // Optional: bi-directional relation to lessons (lessons.category_id)
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY, orphanRemoval = false)
    @Builder.Default
    private List<Lesson> lessons = new ArrayList<>();

    /* ---------- lifecycle hooks ---------- */
    @PrePersist
    private void prePersist() {
        if (this.name != null) this.name = this.name.trim();
        if (this.iconUrl != null) this.iconUrl = this.iconUrl.trim();
        if (this.isActive == null) this.isActive = true;
    }
}