package me.aydgn.MorseMate.admin.service;

import jakarta.persistence.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.admin.dto.EntityFieldMetadata;
import me.aydgn.MorseMate.admin.dto.EntityMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for extracting and managing entity metadata.
 * Provides Django-style introspection for admin panel.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EntityMetadataService {

    private final Map<String, EntityMetadata> entityMetadataCache = new HashMap<>();

    /**
     * Get metadata for an entity by name.
     *
     * @param entityName Entity name (e.g., "User", "Category")
     * @return Entity metadata
     */
    public EntityMetadata getEntityMetadata(String entityName) {
        return entityMetadataCache.get(entityName);
    }

    /**
     * Get all managed entities.
     *
     * @return List of entity metadata
     */
    public List<EntityMetadata> getAllEntities() {
        return new ArrayList<>(entityMetadataCache.values());
    }

    /**
     * Register an entity for admin management.
     *
     * @param entityClass     Entity class
     * @param repositoryClass Repository class
     */
    public void registerEntity(Class<?> entityClass, Class<? extends JpaRepository> repositoryClass) {
        String entityName = entityClass.getSimpleName();

        log.info("Registering entity: {}", entityName);

        EntityMetadata metadata = EntityMetadata.builder()
                .name(entityName)
                .displayName(entityName)
                .entityClass(entityClass)
                .listFields(extractListFields(entityClass))
                .formFields(extractFormFields(entityClass))
                .repositoryBeanName(getRepositoryBeanName(repositoryClass))
                .icon(getDefaultIcon(entityName))
                .manageable(true)
                .displayOrder(entityMetadataCache.size())
                .description("Manage " + entityName + " entities")
                .build();

        entityMetadataCache.put(entityName, metadata);
    }

    /**
     * Extract fields to show in list view.
     * Excludes collection fields and large text fields.
     */
    private List<EntityFieldMetadata> extractListFields(Class<?> entityClass) {
        List<EntityFieldMetadata> fields = new ArrayList<>();

        for (Field field : getAllFields(entityClass)) {
            // Skip ID, collections, and internal fields
            if (field.getName().equals("id") || field.getName().startsWith("$$")) {
                continue;
            }

            // Skip collections in list view
            if (Collection.class.isAssignableFrom(field.getType())) {
                continue;
            }

            // Skip large text fields in list view
            if (field.isAnnotationPresent(Lob.class) || field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);
                if (column != null && column.columnDefinition() != null && column.columnDefinition().contains("TEXT")) {
                    continue;
                }
            }

            EntityFieldMetadata fieldMetadata = extractFieldMetadata(field, entityClass);
            if (fieldMetadata != null) {
                fields.add(fieldMetadata);
            }
        }

        return fields;
    }

    /**
     * Extract fields to show in forms (create/edit).
     * Excludes ID, createdAt, updatedAt, and other read-only fields.
     */
    private List<EntityFieldMetadata> extractFormFields(Class<?> entityClass) {
        List<EntityFieldMetadata> fields = new ArrayList<>();

        for (Field field : getAllFields(entityClass)) {
            // Skip auto-generated and read-only fields
            if (field.getName().equals("id") ||
                field.getName().equals("createdAt") ||
                field.getName().equals("updatedAt") ||
                field.getName().equals("deletedAt") ||
                field.getName().startsWith("$$")) {
                continue;
            }

            EntityFieldMetadata fieldMetadata = extractFieldMetadata(field, entityClass);
            if (fieldMetadata != null) {
                fields.add(fieldMetadata);
            }
        }

        return fields;
    }

    /**
     * Extract metadata for a single field.
     */
    private EntityFieldMetadata extractFieldMetadata(Field field, Class<?> entityClass) {
        field.setAccessible(true);

        String fieldName = field.getName();
        String displayName = toDisplayName(fieldName);
        EntityFieldMetadata.FieldType fieldType = determineFieldType(field);
        boolean required = isRequired(field);
        Method getter = findGetter(entityClass, field);

        EntityFieldMetadata.EntityFieldMetadataBuilder builder = EntityFieldMetadata.builder()
                .name(fieldName)
                .displayName(displayName)
                .type(fieldType)
                .required(required)
                .field(field)
                .getter(getter);

        // Handle enums
        if (fieldType == EntityFieldMetadata.FieldType.ENUM) {
            Class<?> enumClass = field.getType();
            Object[] enumConstants = enumClass.getEnumConstants();
            List<String> options = Arrays.stream(enumConstants)
                    .map(Object::toString)
                    .collect(Collectors.toList());
            builder.options(options);
        }

        // Handle relations
        if (fieldType == EntityFieldMetadata.FieldType.RELATION) {
            Class<?> relatedClass = field.getType();
            builder.relatedEntityName(relatedClass.getSimpleName());
            // Relation options will be loaded dynamically when needed
        }

        // Handle collections
        if (fieldType == EntityFieldMetadata.FieldType.COLLECTION) {
            if (field.getGenericType() instanceof ParameterizedType) {
                ParameterizedType paramType = (ParameterizedType) field.getGenericType();
                Class<?> relatedClass = (Class<?>) paramType.getActualTypeArguments()[0];
                builder.relatedEntityName(relatedClass.getSimpleName());
            }
        }

        return builder.build();
    }

    /**
     * Determine field type for rendering.
     */
    private EntityFieldMetadata.FieldType determineFieldType(Field field) {
        Class<?> type = field.getType();

        // Boolean
        if (type == boolean.class || type == Boolean.class) {
            return EntityFieldMetadata.FieldType.BOOLEAN;
        }

        // Number types
        if (type == int.class || type == Integer.class ||
            type == long.class || type == Long.class) {
            return EntityFieldMetadata.FieldType.INTEGER;
        }

        if (type == float.class || type == Float.class ||
            type == double.class || type == Double.class) {
            return EntityFieldMetadata.FieldType.NUMBER;
        }

        // Date/Time
        if (type == LocalDate.class) {
            return EntityFieldMetadata.FieldType.DATE;
        }

        if (type == LocalDateTime.class) {
            return EntityFieldMetadata.FieldType.DATETIME;
        }

        // Enum
        if (type.isEnum()) {
            return EntityFieldMetadata.FieldType.ENUM;
        }

        // Email (by field name)
        if (field.getName().toLowerCase().contains("email")) {
            return EntityFieldMetadata.FieldType.EMAIL;
        }

        // Relations
        if (field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(OneToOne.class)) {
            return EntityFieldMetadata.FieldType.RELATION;
        }

        // Collections
        if (Collection.class.isAssignableFrom(type)) {
            return EntityFieldMetadata.FieldType.COLLECTION;
        }

        // Text (for @Lob or large columns)
        if (field.isAnnotationPresent(Lob.class)) {
            return EntityFieldMetadata.FieldType.TEXT;
        }

        if (field.isAnnotationPresent(Column.class)) {
            Column column = field.getAnnotation(Column.class);
            if (column.length() > 500) {
                return EntityFieldMetadata.FieldType.TEXT;
            }
        }

        // Default: String
        return EntityFieldMetadata.FieldType.STRING;
    }

    /**
     * Check if field is required.
     */
    private boolean isRequired(Field field) {
        if (field.isAnnotationPresent(Column.class)) {
            Column column = field.getAnnotation(Column.class);
            return !column.nullable();
        }

        if (field.isAnnotationPresent(ManyToOne.class)) {
            ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
            return !manyToOne.optional();
        }

        if (field.isAnnotationPresent(OneToOne.class)) {
            OneToOne oneToOne = field.getAnnotation(OneToOne.class);
            return !oneToOne.optional();
        }

        return false;
    }

    /**
     * Find getter method for a field.
     */
    private Method findGetter(Class<?> clazz, Field field) {
        String fieldName = field.getName();
        String getterName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

        // Try boolean getter for boolean fields
        if (field.getType() == boolean.class || field.getType() == Boolean.class) {
            String boolGetterName = "is" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            try {
                return clazz.getMethod(boolGetterName);
            } catch (NoSuchMethodException ignored) {
            }
        }

        try {
            return clazz.getMethod(getterName);
        } catch (NoSuchMethodException e) {
            log.warn("Getter not found for field: {} in class: {}", fieldName, clazz.getSimpleName());
            return null;
        }
    }

    /**
     * Get all fields including inherited ones.
     */
    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;

        while (current != null && current != Object.class) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }

        return fields;
    }

    /**
     * Convert field name to display name (camelCase -> Title Case).
     */
    private String toDisplayName(String fieldName) {
        return fieldName.replaceAll("([A-Z])", " $1")
                .trim()
                .substring(0, 1).toUpperCase() + fieldName.replaceAll("([A-Z])", " $1").trim().substring(1);
    }

    /**
     * Get repository bean name from repository class.
     */
    private String getRepositoryBeanName(Class<? extends JpaRepository> repositoryClass) {
        String simpleName = repositoryClass.getSimpleName();
        return simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);
    }

    /**
     * Get default icon for entity based on name.
     */
    private String getDefaultIcon(String entityName) {
        return switch (entityName.toLowerCase()) {
            case "user" -> "fas fa-users";
            case "category" -> "fas fa-folder";
            case "lesson" -> "fas fa-book";
            case "exercise" -> "fas fa-puzzle-piece";
            case "subscription", "usersubscription" -> "fas fa-credit-card";
            case "achievement" -> "fas fa-trophy";
            case "powerup" -> "fas fa-bolt";
            case "leaderboard" -> "fas fa-chart-line";
            default -> "fas fa-database";
        };
    }
}
