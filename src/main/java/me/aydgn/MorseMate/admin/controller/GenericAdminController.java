package me.aydgn.MorseMate.admin.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.admin.dto.EntityFieldMetadata;
import me.aydgn.MorseMate.admin.dto.EntityMetadata;
import me.aydgn.MorseMate.admin.service.EntityMetadataService;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Generic admin controller for CRUD operations on any entity.
 * Inspired by Django Admin - provides automatic CRUD interface for all entities.
 *
 * Note: Uses reflection and raw types for generic entity handling.
 * Suppressing unchecked warnings as type safety is validated at runtime.
 */
@Controller
@RequestMapping("/admin/entity")
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings({"unchecked", "rawtypes"})
public class GenericAdminController {

    private final EntityMetadataService metadataService;
    private final ApplicationContext applicationContext;

    /**
     * List all entities of a type.
     * URL: /admin/entity/{entityName}
     */
    @GetMapping("/{entityName}")
    public String listEntities(@PathVariable String entityName, Model model) {
        try {
            EntityMetadata metadata = metadataService.getEntityMetadata(entityName);
            if (metadata == null) {
                return "redirect:/admin?error=Entity not found: " + entityName;
            }

            // Get repository and fetch all entities
            JpaRepository repository = getRepository(metadata);
            List<?> entities = repository.findAll();

            // Load relation options for fields
            List<EntityFieldMetadata> fields = metadata.getListFields();
            loadRelationOptions(fields, repository);

            model.addAttribute("entityName", entityName);
            model.addAttribute("entities", entities);
            model.addAttribute("fields", fields);
            model.addAttribute("pageTitle", entityName + " List");
            model.addAttribute("activeMenu", entityName);

            return "admin/entity/list";
        } catch (Exception e) {
            log.error("Error listing entities for: " + entityName, e);
            return "redirect:/admin?error=Failed to load entities";
        }
    }

    /**
     * Show create form.
     * URL: /admin/entity/{entityName}/create
     */
    @GetMapping("/{entityName}/create")
    public String createForm(@PathVariable String entityName, Model model) {
        try {
            EntityMetadata metadata = metadataService.getEntityMetadata(entityName);
            if (metadata == null) {
                return "redirect:/admin?error=Entity not found";
            }

            // Create a new empty instance
            Object entity = metadata.getEntityClass().getDeclaredConstructor().newInstance();

            // Load relation options
            List<EntityFieldMetadata> fields = metadata.getFormFields();
            JpaRepository repository = getRepository(metadata);
            loadRelationOptions(fields, repository);

            model.addAttribute("entityName", entityName);
            model.addAttribute("entity", entity);
            model.addAttribute("fields", fields);
            model.addAttribute("isEdit", false);
            model.addAttribute("pageTitle", "Create " + entityName);
            model.addAttribute("activeMenu", entityName);

            return "admin/entity/form";
        } catch (Exception e) {
            log.error("Error showing create form for: " + entityName, e);
            return "redirect:/admin?error=Failed to load create form";
        }
    }

    /**
     * Save new entity.
     * URL: /admin/entity/{entityName}/save
     */
    @PostMapping("/{entityName}/save")
    public String saveEntity(@PathVariable String entityName,
                             @RequestParam java.util.Map<String, String> params,
                             RedirectAttributes redirectAttributes) {
        try {
            EntityMetadata metadata = metadataService.getEntityMetadata(entityName);
            if (metadata == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Entity not found");
                return "redirect:/admin";
            }

            // Create new instance
            Object entity = metadata.getEntityClass().getDeclaredConstructor().newInstance();

            // Populate fields from form data
            populateEntity(entity, params, metadata.getFormFields());

            // Save entity
            JpaRepository repository = getRepository(metadata);
            repository.save(entity);

            redirectAttributes.addFlashAttribute("successMessage", entityName + " created successfully");
            return "redirect:/admin/entity/" + entityName;
        } catch (Exception e) {
            log.error("Error saving entity: " + entityName, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create " + entityName + ": " + e.getMessage());
            return "redirect:/admin/entity/" + entityName + "/create";
        }
    }

    /**
     * Show edit form.
     * URL: /admin/entity/{entityName}/{id}/edit
     */
    @GetMapping("/{entityName}/{id}/edit")
    public String editForm(@PathVariable String entityName,
                           @PathVariable Long id,
                           Model model) {
        try {
            EntityMetadata metadata = metadataService.getEntityMetadata(entityName);
            if (metadata == null) {
                return "redirect:/admin?error=Entity not found";
            }

            // Get entity by ID
            JpaRepository repository = getRepository(metadata);
            Optional<?> entityOpt = repository.findById(id);

            if (entityOpt.isEmpty()) {
                return "redirect:/admin/entity/" + entityName + "?error=Entity not found with ID: " + id;
            }

            Object entity = entityOpt.get();

            // Load relation options
            List<EntityFieldMetadata> fields = metadata.getFormFields();
            loadRelationOptions(fields, repository);

            model.addAttribute("entityName", entityName);
            model.addAttribute("entity", entity);
            model.addAttribute("fields", fields);
            model.addAttribute("isEdit", true);
            model.addAttribute("pageTitle", "Edit " + entityName);
            model.addAttribute("activeMenu", entityName);

            return "admin/entity/form";
        } catch (Exception e) {
            log.error("Error showing edit form for: " + entityName + " ID: " + id, e);
            return "redirect:/admin/entity/" + entityName + "?error=Failed to load entity";
        }
    }

    /**
     * Update existing entity.
     * URL: /admin/entity/{entityName}/{id}/update
     */
    @PostMapping("/{entityName}/{id}/update")
    public String updateEntity(@PathVariable String entityName,
                               @PathVariable Long id,
                               @RequestParam java.util.Map<String, String> params,
                               RedirectAttributes redirectAttributes) {
        try {
            EntityMetadata metadata = metadataService.getEntityMetadata(entityName);
            if (metadata == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Entity not found");
                return "redirect:/admin";
            }

            // Get existing entity
            JpaRepository repository = getRepository(metadata);
            Optional<?> entityOpt = repository.findById(id);

            if (entityOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Entity not found with ID: " + id);
                return "redirect:/admin/entity/" + entityName;
            }

            Object entity = entityOpt.get();

            // Update fields from form data
            populateEntity(entity, params, metadata.getFormFields());

            // Save entity
            repository.save(entity);

            redirectAttributes.addFlashAttribute("successMessage", entityName + " updated successfully");
            return "redirect:/admin/entity/" + entityName;
        } catch (Exception e) {
            log.error("Error updating entity: " + entityName + " ID: " + id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update " + entityName + ": " + e.getMessage());
            return "redirect:/admin/entity/" + entityName + "/" + id + "/edit";
        }
    }

    /**
     * Delete entity.
     * URL: /admin/entity/{entityName}/{id}/delete
     */
    @PostMapping("/{entityName}/{id}/delete")
    public String deleteEntity(@PathVariable String entityName,
                               @PathVariable Long id,
                               RedirectAttributes redirectAttributes) {
        try {
            EntityMetadata metadata = metadataService.getEntityMetadata(entityName);
            if (metadata == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Entity not found");
                return "redirect:/admin";
            }

            JpaRepository repository = getRepository(metadata);
            repository.deleteById(id);

            redirectAttributes.addFlashAttribute("successMessage", entityName + " deleted successfully");
            return "redirect:/admin/entity/" + entityName;
        } catch (Exception e) {
            log.error("Error deleting entity: " + entityName + " ID: " + id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete " + entityName + ": " + e.getMessage());
            return "redirect:/admin/entity/" + entityName;
        }
    }

    /**
     * View entity details.
     * URL: /admin/entity/{entityName}/{id}
     */
    @GetMapping("/{entityName}/{id}")
    public String viewEntity(@PathVariable String entityName,
                             @PathVariable Long id,
                             Model model) {
        try {
            EntityMetadata metadata = metadataService.getEntityMetadata(entityName);
            if (metadata == null) {
                return "redirect:/admin?error=Entity not found";
            }

            JpaRepository repository = getRepository(metadata);
            Optional<?> entityOpt = repository.findById(id);

            if (entityOpt.isEmpty()) {
                return "redirect:/admin/entity/" + entityName + "?error=Entity not found";
            }

            model.addAttribute("entityName", entityName);
            model.addAttribute("entity", entityOpt.get());
            model.addAttribute("fields", metadata.getFormFields());
            model.addAttribute("pageTitle", entityName + " Details");
            model.addAttribute("activeMenu", entityName);

            return "admin/entity/detail";
        } catch (Exception e) {
            log.error("Error viewing entity: " + entityName + " ID: " + id, e);
            return "redirect:/admin/entity/" + entityName + "?error=Failed to load entity";
        }
    }

    /**
     * Get repository for entity.
     */
    private JpaRepository getRepository(EntityMetadata metadata) {
        return (JpaRepository) applicationContext.getBean(metadata.getRepositoryBeanName());
    }

    /**
     * Populate entity from form parameters using reflection.
     */
    private void populateEntity(Object entity, java.util.Map<String, String> params, List<EntityFieldMetadata> fields) throws Exception {
        for (EntityFieldMetadata fieldMeta : fields) {
            String fieldName = fieldMeta.getName();
            String value = params.get(fieldName);

            if (value == null || value.trim().isEmpty()) {
                // Handle checkboxes (unchecked checkboxes don't send values)
                if (fieldMeta.getType() == EntityFieldMetadata.FieldType.BOOLEAN) {
                    setFieldValue(entity, fieldMeta.getField(), false);
                }
                continue;
            }

            Object convertedValue = convertValue(value, fieldMeta, params);
            setFieldValue(entity, fieldMeta.getField(), convertedValue);
        }
    }

    /**
     * Convert string value to appropriate type.
     */
    private Object convertValue(String value, EntityFieldMetadata fieldMeta, java.util.Map<String, String> allParams) throws Exception {
        return switch (fieldMeta.getType()) {
            case STRING, TEXT, EMAIL -> value;
            case INTEGER -> Long.parseLong(value);
            case NUMBER -> Double.parseDouble(value);
            case BOOLEAN -> Boolean.parseBoolean(value) || value.equals("on");
            case ENUM -> Enum.valueOf((Class<Enum>) fieldMeta.getField().getType(), value);
            case DATE -> java.time.LocalDate.parse(value);
            case DATETIME -> java.time.LocalDateTime.parse(value);
            case RELATION -> {
                // Load related entity by ID
                Long relatedId = Long.parseLong(value);
                String relatedEntityName = fieldMeta.getRelatedEntityName();
                EntityMetadata relatedMetadata = metadataService.getEntityMetadata(relatedEntityName);
                if (relatedMetadata != null) {
                    JpaRepository relatedRepo = getRepository(relatedMetadata);
                    Optional<?> related = relatedRepo.findById(relatedId);
                    yield related.orElse(null);
                }
                yield null;
            }
            default -> value;
        };
    }

    /**
     * Set field value using reflection.
     */
    private void setFieldValue(Object entity, Field field, Object value) throws Exception {
        // Try setter first
        String setterName = "set" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
        try {
            Method setter = entity.getClass().getMethod(setterName, field.getType());
            setter.invoke(entity, value);
        } catch (NoSuchMethodException e) {
            // Fall back to direct field access
            field.setAccessible(true);
            field.set(entity, value);
        }
    }

    /**
     * Load relation options for select dropdowns.
     */
    private void loadRelationOptions(List<EntityFieldMetadata> fields, JpaRepository currentRepo) {
        for (EntityFieldMetadata field : fields) {
            if (field.getType() == EntityFieldMetadata.FieldType.RELATION) {
                String relatedEntityName = field.getRelatedEntityName();
                EntityMetadata relatedMetadata = metadataService.getEntityMetadata(relatedEntityName);

                if (relatedMetadata != null) {
                    try {
                        JpaRepository relatedRepo = getRepository(relatedMetadata);
                        List<?> relatedEntities = relatedRepo.findAll();

                        List<EntityFieldMetadata.RelationOption> options = new ArrayList<>();
                        for (Object relatedEntity : relatedEntities) {
                            Method idMethod = relatedEntity.getClass().getMethod("getId");
                            Long id = (Long) idMethod.invoke(relatedEntity);

                            String displayValue = getEntityDisplayValue(relatedEntity);

                            options.add(EntityFieldMetadata.RelationOption.builder()
                                    .id(id)
                                    .displayValue(displayValue)
                                    .build());
                        }

                        field.setRelationOptions(options);
                    } catch (Exception e) {
                        log.warn("Failed to load relation options for: " + relatedEntityName, e);
                    }
                }
            }
        }
    }

    /**
     * Get display value for an entity (tries name, title, or ID).
     */
    private String getEntityDisplayValue(Object entity) {
        try {
            // Try getName()
            try {
                Method nameMethod = entity.getClass().getMethod("getName");
                Object name = nameMethod.invoke(entity);
                if (name != null) return name.toString();
            } catch (NoSuchMethodException ignored) {
            }

            // Try getTitle()
            try {
                Method titleMethod = entity.getClass().getMethod("getTitle");
                Object title = titleMethod.invoke(entity);
                if (title != null) return title.toString();
            } catch (NoSuchMethodException ignored) {
            }

            // Try getUsername()
            try {
                Method usernameMethod = entity.getClass().getMethod("getUsername");
                Object username = usernameMethod.invoke(entity);
                if (username != null) return username.toString();
            } catch (NoSuchMethodException ignored) {
            }

            // Fallback to ID
            Method idMethod = entity.getClass().getMethod("getId");
            Object id = idMethod.invoke(entity);
            return "ID: " + id;
        } catch (Exception e) {
            return entity.toString();
        }
    }
}
