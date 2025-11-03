package me.aydgn.MorseMate.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Metadata for a single entity field.
 * Used for dynamic form generation and table rendering in Django-style admin panel.
 */
@Data
@Builder
public class EntityFieldMetadata {

    /**
     * Field name (e.g., "username", "email")
     */
    private String name;

    /**
     * Display name for UI (e.g., "User Name", "Email Address")
     */
    private String displayName;

    /**
     * Field type for rendering (STRING, TEXT, NUMBER, BOOLEAN, ENUM, DATE, DATETIME, RELATION, COLLECTION)
     */
    private FieldType type;

    /**
     * Is this field required?
     */
    private boolean required;

    /**
     * Java field for reflection access
     */
    private Field field;

    /**
     * Getter method for the field
     */
    private Method getter;

    /**
     * Enum values (if type is ENUM)
     */
    private List<String> options;

    /**
     * Relation options (if type is RELATION)
     */
    private List<RelationOption> relationOptions;

    /**
     * Help text for the field
     */
    private String helpText;

    /**
     * Related entity class name (if type is RELATION or COLLECTION)
     */
    private String relatedEntityName;

    /**
     * Get field value from an entity instance using reflection.
     *
     * @param entity Entity instance
     * @return Field value
     */
    public Object getValue(Object entity) {
        try {
            if (getter != null) {
                return getter.invoke(entity);
            } else if (field != null) {
                field.setAccessible(true);
                return field.get(entity);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get display value for the field (for relations, shows toString() or ID).
     *
     * @param entity Entity instance
     * @return Display-friendly value
     */
    public String getDisplayValue(Object entity) {
        Object value = getValue(entity);
        if (value == null) {
            return "-";
        }

        if (type == FieldType.RELATION) {
            // Try to get a meaningful display value from the related entity
            try {
                Method idMethod = value.getClass().getMethod("getId");
                Object id = idMethod.invoke(value);

                // Try to find a name/title field
                try {
                    Method nameMethod = value.getClass().getMethod("getName");
                    Object name = nameMethod.invoke(value);
                    return name + " (ID: " + id + ")";
                } catch (NoSuchMethodException e1) {
                    try {
                        Method titleMethod = value.getClass().getMethod("getTitle");
                        Object title = titleMethod.invoke(value);
                        return title + " (ID: " + id + ")";
                    } catch (NoSuchMethodException e2) {
                        return "ID: " + id;
                    }
                }
            } catch (Exception e) {
                return value.toString();
            }
        }

        return value.toString();
    }

    /**
     * Field types for rendering
     */
    public enum FieldType {
        STRING,     // Basic string input
        TEXT,       // Textarea for long text
        NUMBER,     // Decimal number input
        INTEGER,    // Integer number input
        BOOLEAN,    // Checkbox
        ENUM,       // Select dropdown with enum values
        DATE,       // Date picker
        DATETIME,   // DateTime picker
        EMAIL,      // Email input
        RELATION,   // Foreign key (ManyToOne, OneToOne)
        COLLECTION  // Collection (OneToMany, ManyToMany)
    }

    /**
     * Relation option for select dropdowns
     */
    @Data
    @Builder
    public static class RelationOption {
        private Long id;
        private String displayValue;
    }
}
