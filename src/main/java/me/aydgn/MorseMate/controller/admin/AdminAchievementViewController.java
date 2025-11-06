package me.aydgn.MorseMate.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.dto.response.AchievementResponse;
import me.aydgn.MorseMate.dto.view.AchievementForm;
import me.aydgn.MorseMate.exception.ResourceNotFoundException;
import me.aydgn.MorseMate.service.AchievementService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Admin controller for achievement management.
 */
@Controller
@RequestMapping("/admin/achievements")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminAchievementViewController extends AbstractAdminPageController {

    private final AchievementService achievementService;

    @GetMapping
    public String listAchievements(@RequestParam(value = "editId", required = false) Long editId,
                                   Model model) {
        List<AchievementResponse> achievements = achievementService.getAllAchievements();
        model.addAttribute("achievements", achievements);

        if (!model.containsAttribute("achievementForm")) {
            model.addAttribute("achievementForm", new AchievementForm());
        }

        Long editingId = resolveEditingId(editId, model);

        if (editingId != null) {
            model.addAttribute("editingId", editingId);
            if (!model.containsAttribute("editForm")) {
                try {
                    AchievementResponse achievement = achievementService.getAchievementById(editingId);
                    model.addAttribute("editForm", AchievementForm.from(achievement));
                } catch (ResourceNotFoundException ex) {
                    log.warn("Requested editId {} not found", editingId, ex);
                    model.addAttribute("errorMessage", "Selected achievement no longer exists.");
                    model.addAttribute("editingId", null);
                }
            }
        }

        return render(model, AdminPage.ACHIEVEMENTS);
    }

    @PostMapping
    public String createAchievement(@Valid @ModelAttribute("achievementForm") AchievementForm form,
                                    BindingResult bindingResult,
                                    RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            preserveFormState("achievementForm", form, bindingResult, redirectAttributes);
            redirectAttributes.addFlashAttribute("errorMessage", "Please correct the highlighted errors.");
            return AdminPage.ACHIEVEMENTS.redirect();
        }

        try {
            achievementService.createAchievement(form.toCreateRequest());
            redirectAttributes.addFlashAttribute("successMessage", "Achievement created successfully.");
        } catch (Exception ex) {
            log.error("Unexpected error creating achievement", ex);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create achievement. Please try again.");
            redirectAttributes.addFlashAttribute("achievementForm", form);
        }

        return AdminPage.ACHIEVEMENTS.redirect();
    }

    @PostMapping("/{id}/update")
    public String updateAchievement(@PathVariable("id") Long id,
                                    @Valid @ModelAttribute("editForm") AchievementForm form,
                                    BindingResult bindingResult,
                                    RedirectAttributes redirectAttributes) {
        form.setId(id);

        if (bindingResult.hasErrors()) {
            preserveFormState("editForm", form, bindingResult, redirectAttributes);
            preserveEditingId(id, redirectAttributes);
            redirectAttributes.addFlashAttribute("errorMessage", "Please correct the highlighted errors.");
            return AdminPage.ACHIEVEMENTS.redirect();
        }

        try {
            achievementService.updateAchievement(id, form.toUpdateRequest());
            redirectAttributes.addFlashAttribute("successMessage", "Achievement updated successfully.");
        } catch (ResourceNotFoundException ex) {
            log.warn("Attempted to update missing achievement {}", id, ex);
            redirectAttributes.addFlashAttribute("errorMessage", "Achievement no longer exists.");
        } catch (Exception ex) {
            log.error("Unexpected error updating achievement {}", id, ex);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update achievement. Please try again.");
            redirectAttributes.addFlashAttribute("editForm", form);
            preserveEditingId(id, redirectAttributes);
        }

        return AdminPage.ACHIEVEMENTS.redirect();
    }

    @PostMapping("/{id}/delete")
    public String deleteAchievement(@PathVariable("id") Long id,
                                    RedirectAttributes redirectAttributes) {
        try {
            achievementService.deleteAchievement(id);
            redirectAttributes.addFlashAttribute("successMessage", "Achievement deleted successfully.");
        } catch (ResourceNotFoundException ex) {
            log.warn("Attempted to delete missing achievement {}", id, ex);
            redirectAttributes.addFlashAttribute("errorMessage", "Achievement no longer exists.");
        } catch (Exception ex) {
            log.error("Unexpected error deleting achievement {}", id, ex);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete achievement. Please try again.");
        }

        return AdminPage.ACHIEVEMENTS.redirect();
    }
}
