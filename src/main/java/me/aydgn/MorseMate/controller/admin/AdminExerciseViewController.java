package me.aydgn.MorseMate.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.dto.response.ExerciseResponse;
import me.aydgn.MorseMate.dto.response.LessonResponse;
import me.aydgn.MorseMate.dto.view.ExerciseForm;
import me.aydgn.MorseMate.entity.Exercise;
import me.aydgn.MorseMate.exception.InvalidOperationException;
import me.aydgn.MorseMate.exception.ResourceNotFoundException;
import me.aydgn.MorseMate.service.ExerciseService;
import me.aydgn.MorseMate.service.LessonService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;

/**
 * Admin controller for exercise management.
 */
@Controller
@RequestMapping("/admin/exercises")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminExerciseViewController extends AbstractAdminPageController {

    private final ExerciseService exerciseService;
    private final LessonService lessonService;

    @GetMapping
    public String listExercises(@RequestParam(value = "lessonId", required = false) Long lessonId,
                                @RequestParam(value = "editId", required = false) Long editId,
                                @RequestParam(value = "page", defaultValue = "0") int page,
                                @RequestParam(value = "size", defaultValue = "20") int size,
                                Model model) {
        int pageNumber = Math.max(page, 0);
        int pageSize = Math.min(Math.max(size, 1), 50);

        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.ASC, "id"));
        Page<ExerciseResponse> exercisePage;
        try {
            exercisePage = exerciseService.getExercisePage(lessonId, pageRequest);
        } catch (Exception ex) {
            log.error("Failed to load exercises (lessonId={}, page={}, size={})", lessonId, pageNumber, pageSize, ex);
            exercisePage = Page.empty(pageRequest);
            model.addAttribute("loadError", "Unable to load exercise data right now. Please try again later.");
        }

        List<LessonResponse> lessons = Collections.emptyList();
        try {
            lessons = lessonService.getAllLessons();
        } catch (Exception ex) {
            log.error("Failed to load lessons for exercise admin view", ex);
        }

        model.addAttribute("exercisePage", exercisePage);
        model.addAttribute("exercises", exercisePage.getContent());
        model.addAttribute("totalPages", exercisePage.getTotalPages());
        model.addAttribute("currentPage", pageNumber);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("lessonFilter", lessonId);
        model.addAttribute("lessons", lessons);
        model.addAttribute("exerciseTypes", Exercise.Type.values());
        model.addAttribute("exerciseDifficulties", Exercise.Difficulty.values());

        if (!model.containsAttribute("exerciseForm")) {
            model.addAttribute("exerciseForm", new ExerciseForm());
        }

        Long editingId = resolveEditingId(editId, model);
        if (editingId != null) {
            model.addAttribute("editingId", editingId);
            if (!model.containsAttribute("editForm")) {
                try {
                    ExerciseResponse exercise = exerciseService.getExerciseDetails(editingId);
                    model.addAttribute("editForm", ExerciseForm.from(exercise));
                } catch (ResourceNotFoundException ex) {
                    log.warn("Requested exercise editId {} not found", editingId, ex);
                    model.addAttribute("errorMessage", "Selected exercise no longer exists.");
                    model.addAttribute("editingId", null);
                }
            }
        }

        return render(model, AdminPage.EXERCISES);
    }

    @PostMapping
    public String createExercise(@Valid @ModelAttribute("exerciseForm") ExerciseForm form,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            preserveFormState("exerciseForm", form, bindingResult, redirectAttributes);
            redirectAttributes.addFlashAttribute("errorMessage", "Please correct the highlighted errors.");
            return AdminPage.EXERCISES.redirect();
        }

        try {
            exerciseService.createExercise(form.toCreateRequest());
            redirectAttributes.addFlashAttribute("successMessage", "Exercise created successfully.");
        } catch (InvalidOperationException | ResourceNotFoundException ex) {
            log.warn("Failed to create exercise for lesson {}: {}", form.getLessonId(), ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("exerciseForm", form);
        } catch (Exception ex) {
            log.error("Unexpected error creating exercise", ex);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create exercise. Please try again.");
            redirectAttributes.addFlashAttribute("exerciseForm", form);
        }

        return AdminPage.EXERCISES.redirect();
    }

    @PostMapping("/{id}/update")
    public String updateExercise(@PathVariable("id") Long id,
                                 @Valid @ModelAttribute("editForm") ExerciseForm form,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        form.setId(id);

        if (bindingResult.hasErrors()) {
            preserveFormState("editForm", form, bindingResult, redirectAttributes);
            preserveEditingId(id, redirectAttributes);
            redirectAttributes.addFlashAttribute("errorMessage", "Please correct the highlighted errors.");
            return AdminPage.EXERCISES.redirect();
        }

        try {
            exerciseService.updateExercise(id, form.toUpdateRequest());
            redirectAttributes.addFlashAttribute("successMessage", "Exercise updated successfully.");
        } catch (InvalidOperationException | ResourceNotFoundException ex) {
            log.warn("Failed to update exercise {}: {}", id, ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("editForm", form);
            preserveEditingId(id, redirectAttributes);
        } catch (Exception ex) {
            log.error("Unexpected error updating exercise {}", id, ex);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update exercise. Please try again.");
            redirectAttributes.addFlashAttribute("editForm", form);
            preserveEditingId(id, redirectAttributes);
        }

        return AdminPage.EXERCISES.redirect();
    }

    @PostMapping("/{id}/delete")
    public String deleteExercise(@PathVariable("id") Long id,
                                 RedirectAttributes redirectAttributes) {
        try {
            exerciseService.deleteExercise(id);
            redirectAttributes.addFlashAttribute("successMessage", "Exercise deleted successfully.");
        } catch (ResourceNotFoundException ex) {
            log.warn("Attempted to delete missing exercise {}", id, ex);
            redirectAttributes.addFlashAttribute("errorMessage", "Exercise no longer exists.");
        } catch (Exception ex) {
            log.error("Unexpected error deleting exercise {}", id, ex);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete exercise. Please try again.");
        }

        return AdminPage.EXERCISES.redirect();
    }
}
