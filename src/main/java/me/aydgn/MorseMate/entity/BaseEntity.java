package me.aydgn.MorseMate.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import jakarta.persistence.EntityListeners;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Base entity class providing auditing and soft delete capabilities
 * All entities extending this class will have:
 * - createdAt: Timestamp when entity was created
 * - updatedAt: Timestamp when entity was last updated
 * - deletedAt: Timestamp when entity was soft deleted (null if active)
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
public abstract class BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Soft delete timestamp
     * If null, entity is active
     * If not null, entity is considered deleted
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * Check if entity is deleted (soft delete)
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * Mark entity as deleted (soft delete)
     */
    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * Restore a soft-deleted entity
     */
    public void restore() {
        this.deletedAt = null;
    }
}
