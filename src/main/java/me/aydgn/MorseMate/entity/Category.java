package me.aydgn.MorseMate.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
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

    // SQL: icon VARCHAR(50)
    @Size(max = 50)
    @Column(name = "icon", length = 50)
    private String icon;

    // SQL: order_index INTEGER
    @Column(name = "order_index")
    private Integer orderIndex;

    // SQL: created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Optional: bi-directional relation to lessons (lessons.category_id)
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY, orphanRemoval = false)
    @Builder.Default
    private List<Lesson> lessons = new ArrayList<>();

    /* ---------- lifecycle hooks ---------- */
    @PrePersist
    private void prePersist() {
        // DB default da verir; uygulama tarafında null ise set etmek faydalı
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.name != null) this.name = this.name.trim();
        if (this.icon != null) this.icon = this.icon.trim();
    }
}