package me.aydgn.MorseMate.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Metadata for an entity class.
 * Contains all information needed for Django-style admin panel CRUD operations.
 */
@Data
@Builder
public class EntityMetadata {

    /**
     * Entity name (e.g., "User", "Category")
     */
    private String name;

    /**
     * Display name for UI (e.g., "User Management", "Category List")
     */
    private String displayName;

    /**
     * Entity class
     */
    private Class<?> entityClass;

    /**
     * List of fields to display in list view
     */
    private List<EntityFieldMetadata> listFields;

    /**
     * List of fields to show in forms (create/edit)
     */
    private List<EntityFieldMetadata> formFields;

    /**
     * Repository bean name for data access
     */
    private String repositoryBeanName;

    /**
     * Icon class for UI (e.g., "fas fa-users")
     */
    private String icon;

    /**
     * Is this entity manageable through admin panel?
     */
    private boolean manageable;

    /**
     * Order for display in menu
     */
    private int displayOrder;

    /**
     * Description of the entity
     */
    private String description;
}
