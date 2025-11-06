package me.aydgn.MorseMate.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.dto.response.CategoryResponse;
import me.aydgn.MorseMate.dto.view.CategoryForm;
import me.aydgn.MorseMate.exception.DuplicateResourceException;
import me.aydgn.MorseMate.exception.InvalidOperationException;
import me.aydgn.MorseMate.exception.ResourceNotFoundException;
import me.aydgn.MorseMate.service.CategoryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Spring MVC controller for admin category management.
 * Replaces the previous JSF-based AdminFaces implementation.
 */
@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminCategoryViewController extends AbstractAdminPageController {

    private final CategoryService categoryService;

    @GetMapping
    public String listCategories(@RequestParam(value = "editId", required = false) Long editId,
                                 Model model) {
        List<CategoryResponse> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);

        if (!model.containsAttribute("categoryForm")) {
            model.addAttribute("categoryForm", new CategoryForm());
        }

        Long editingId = resolveEditingId(editId, model);

        if (editingId != null) {
            model.addAttribute("editingId", editingId);
            if (!model.containsAttribute("editForm")) {
                try {
                    CategoryResponse category = categoryService.getCategoryById(editingId);
                    model.addAttribute("editForm", CategoryForm.from(category));
                } catch (ResourceNotFoundException ex) {
                    log.warn("Requested editId {} not found", editingId, ex);
                    model.addAttribute("errorMessage", "Selected category no longer exists.");
                    model.addAttribute("editingId", null);
                }
            }
        }

        return render(model, AdminPage.CATEGORIES);
    }

    @PostMapping
    public String createCategory(@Valid @ModelAttribute("categoryForm") CategoryForm form,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            preserveFormState("categoryForm", form, bindingResult, redirectAttributes);
            redirectAttributes.addFlashAttribute("errorMessage", "Please correct the highlighted errors.");
            return AdminPage.CATEGORIES.redirect();
        }

        try {
            categoryService.createCategory(form.toCreateRequest());
            redirectAttributes.addFlashAttribute("successMessage", "Category created successfully.");
        } catch (DuplicateResourceException ex) {
            log.warn("Duplicate category creation attempt for name {}", form.getName(), ex);
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("categoryForm", form);
        } catch (Exception ex) {
            log.error("Unexpected error creating category", ex);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create category. Please try again.");
            redirectAttributes.addFlashAttribute("categoryForm", form);
        }

        return AdminPage.CATEGORIES.redirect();
    }

    @PostMapping("/{id}/update")
    public String updateCategory(@PathVariable("id") Long id,
                                 @Valid @ModelAttribute("editForm") CategoryForm form,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        form.setId(id);

        if (bindingResult.hasErrors()) {
            preserveFormState("editForm", form, bindingResult, redirectAttributes);
            preserveEditingId(id, redirectAttributes);
            redirectAttributes.addFlashAttribute("errorMessage", "Please correct the highlighted errors.");
            return AdminPage.CATEGORIES.redirect();
        }

        try {
            categoryService.updateCategory(id, form.toUpdateRequest());
            redirectAttributes.addFlashAttribute("successMessage", "Category updated successfully.");
        } catch (DuplicateResourceException ex) {
            log.warn("Duplicate category update attempt for name {}", form.getName(), ex);
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("editForm", form);
            preserveEditingId(id, redirectAttributes);
        } catch (ResourceNotFoundException ex) {
            log.warn("Attempted to update missing category {}", id, ex);
            redirectAttributes.addFlashAttribute("errorMessage", "Category no longer exists.");
        } catch (Exception ex) {
            log.error("Unexpected error updating category {}", id, ex);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update category. Please try again.");
            redirectAttributes.addFlashAttribute("editForm", form);
            preserveEditingId(id, redirectAttributes);
        }

        return AdminPage.CATEGORIES.redirect();
    }

    @PostMapping("/{id}/delete")
    public String deleteCategory(@PathVariable("id") Long id,
                                 RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("successMessage", "Category deleted successfully.");
        } catch (InvalidOperationException ex) {
            log.warn("Failed to delete category {} due to dependent data", id, ex);
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (ResourceNotFoundException ex) {
            log.warn("Attempted to delete missing category {}", id, ex);
            redirectAttributes.addFlashAttribute("errorMessage", "Category no longer exists.");
        } catch (Exception ex) {
            log.error("Unexpected error deleting category {}", id, ex);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete category. Please try again.");
        }

        return AdminPage.CATEGORIES.redirect();
    }
}
