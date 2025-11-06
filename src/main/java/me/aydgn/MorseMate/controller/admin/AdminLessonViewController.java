package me.aydgn.MorseMate.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.dto.response.CategoryResponse;
import me.aydgn.MorseMate.dto.response.LessonResponse;
import me.aydgn.MorseMate.dto.view.LessonForm;
import me.aydgn.MorseMate.exception.InvalidOperationException;
import me.aydgn.MorseMate.exception.ResourceNotFoundException;
import me.aydgn.MorseMate.service.CategoryService;
import me.aydgn.MorseMate.service.LessonService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Admin controller for lesson management.
 */
@Controller
@RequestMapping("/admin/lessons")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminLessonViewController extends AbstractAdminPageController {

    private final LessonService lessonService;
    private final CategoryService categoryService;

    @GetMapping
    public String listLessons(@RequestParam(value = "editId", required = false) Long editId,
                              Model model) {
        // Get all lessons by fetching from all categories
        List<CategoryResponse> categories = categoryService.getAllCategories();
        List<LessonResponse> lessons = new java.util.ArrayList<>();
        for (CategoryResponse category : categories) {
            lessons.addAll(lessonService.getLessonsByCategoryId(category.getId()));
        }

        model.addAttribute("lessons", lessons);
        model.addAttribute("categories", categories);

        if (!model.containsAttribute("lessonForm")) {
            model.addAttribute("lessonForm", new LessonForm());
        }

        Long editingId = resolveEditingId(editId, model);

        if (editingId != null) {
            model.addAttribute("editingId", editingId);
            if (!model.containsAttribute("editForm")) {
                try {
                    LessonResponse lesson = lessonService.getLessonById(editingId, false);
                    model.addAttribute("editForm", LessonForm.from(lesson));
                } catch (ResourceNotFoundException ex) {
                    log.warn("Requested editId {} not found", editingId, ex);
                    model.addAttribute("errorMessage", "Selected lesson no longer exists.");
                    model.addAttribute("editingId", null);
                }
            }
        }

        return render(model, AdminPage.LESSONS);
    }

    @PostMapping
    public String createLesson(@Valid @ModelAttribute("lessonForm") LessonForm form,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            preserveFormState("lessonForm", form, bindingResult, redirectAttributes);
            redirectAttributes.addFlashAttribute("errorMessage", "Please correct the highlighted errors.");
            return AdminPage.LESSONS.redirect();
        }

        try {
            lessonService.createLesson(form.toCreateRequest());
            redirectAttributes.addFlashAttribute("successMessage", "Lesson created successfully.");
        } catch (Exception ex) {
            log.error("Unexpected error creating lesson", ex);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create lesson. Please try again.");
            redirectAttributes.addFlashAttribute("lessonForm", form);
        }

        return AdminPage.LESSONS.redirect();
    }

    @PostMapping("/{id}/update")
    public String updateLesson(@PathVariable("id") Long id,
                               @Valid @ModelAttribute("editForm") LessonForm form,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        form.setId(id);

        if (bindingResult.hasErrors()) {
            preserveFormState("editForm", form, bindingResult, redirectAttributes);
            preserveEditingId(id, redirectAttributes);
            redirectAttributes.addFlashAttribute("errorMessage", "Please correct the highlighted errors.");
            return AdminPage.LESSONS.redirect();
        }

        try {
            lessonService.updateLesson(id, form.toUpdateRequest());
            redirectAttributes.addFlashAttribute("successMessage", "Lesson updated successfully.");
        } catch (ResourceNotFoundException ex) {
            log.warn("Attempted to update missing lesson {}", id, ex);
            redirectAttributes.addFlashAttribute("errorMessage", "Lesson no longer exists.");
        } catch (Exception ex) {
            log.error("Unexpected error updating lesson {}", id, ex);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update lesson. Please try again.");
            redirectAttributes.addFlashAttribute("editForm", form);
            preserveEditingId(id, redirectAttributes);
        }

        return AdminPage.LESSONS.redirect();
    }

    @PostMapping("/{id}/delete")
    public String deleteLesson(@PathVariable("id") Long id,
                               RedirectAttributes redirectAttributes) {
        try {
            lessonService.deleteLesson(id);
            redirectAttributes.addFlashAttribute("successMessage", "Lesson deleted successfully.");
        } catch (InvalidOperationException ex) {
            log.warn("Failed to delete lesson {} due to invalid state", id, ex);
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (ResourceNotFoundException ex) {
            log.warn("Attempted to delete missing lesson {}", id, ex);
            redirectAttributes.addFlashAttribute("errorMessage", "Lesson no longer exists.");
        } catch (Exception ex) {
            log.error("Unexpected error deleting lesson {}", id, ex);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete lesson. Please try again.");
        }

        return AdminPage.LESSONS.redirect();
    }
}
