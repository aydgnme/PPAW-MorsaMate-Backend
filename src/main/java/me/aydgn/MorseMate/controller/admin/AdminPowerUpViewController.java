package me.aydgn.MorseMate.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.dto.response.PowerUpResponse;
import me.aydgn.MorseMate.dto.view.PowerUpForm;
import me.aydgn.MorseMate.exception.ResourceNotFoundException;
import me.aydgn.MorseMate.service.PowerUpService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Admin controller for power-up management.
 */
@Controller
@RequestMapping("/admin/powerups")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminPowerUpViewController extends AbstractAdminPageController {

    private final PowerUpService powerUpService;

    @GetMapping
    public String listPowerUps(@RequestParam(value = "editId", required = false) Long editId,
                               Model model) {
        List<PowerUpResponse> powerups = powerUpService.getAllPowerUps();
        model.addAttribute("powerups", powerups);

        if (!model.containsAttribute("powerupForm")) {
            model.addAttribute("powerupForm", new PowerUpForm());
        }

        Long editingId = resolveEditingId(editId, model);

        if (editingId != null) {
            model.addAttribute("editingId", editingId);
            if (!model.containsAttribute("editForm")) {
                try {
                    PowerUpResponse powerup = powerUpService.getPowerUpById(editingId);
                    model.addAttribute("editForm", PowerUpForm.from(powerup));
                } catch (ResourceNotFoundException ex) {
                    log.warn("Requested editId {} not found", editingId, ex);
                    model.addAttribute("errorMessage", "Selected power-up no longer exists.");
                    model.addAttribute("editingId", null);
                }
            }
        }

        return render(model, AdminPage.POWERUPS);
    }

    @PostMapping
    public String createPowerUp(@Valid @ModelAttribute("powerupForm") PowerUpForm form,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            preserveFormState("powerupForm", form, bindingResult, redirectAttributes);
            redirectAttributes.addFlashAttribute("errorMessage", "Please correct the highlighted errors.");
            return AdminPage.POWERUPS.redirect();
        }

        try {
            powerUpService.createPowerUp(form.toCreateRequest());
            redirectAttributes.addFlashAttribute("successMessage", "Power-up created successfully.");
        } catch (Exception ex) {
            log.error("Unexpected error creating power-up", ex);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create power-up. Please try again.");
            redirectAttributes.addFlashAttribute("powerupForm", form);
        }

        return AdminPage.POWERUPS.redirect();
    }

    @PostMapping("/{id}/update")
    public String updatePowerUp(@PathVariable("id") Long id,
                                @Valid @ModelAttribute("editForm") PowerUpForm form,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        form.setId(id);

        if (bindingResult.hasErrors()) {
            preserveFormState("editForm", form, bindingResult, redirectAttributes);
            preserveEditingId(id, redirectAttributes);
            redirectAttributes.addFlashAttribute("errorMessage", "Please correct the highlighted errors.");
            return AdminPage.POWERUPS.redirect();
        }

        try {
            powerUpService.updatePowerUp(id, form.toUpdateRequest());
            redirectAttributes.addFlashAttribute("successMessage", "Power-up updated successfully.");
        } catch (ResourceNotFoundException ex) {
            log.warn("Attempted to update missing power-up {}", id, ex);
            redirectAttributes.addFlashAttribute("errorMessage", "Power-up no longer exists.");
        } catch (Exception ex) {
            log.error("Unexpected error updating power-up {}", id, ex);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update power-up. Please try again.");
            redirectAttributes.addFlashAttribute("editForm", form);
            preserveEditingId(id, redirectAttributes);
        }

        return AdminPage.POWERUPS.redirect();
    }

    @PostMapping("/{id}/delete")
    public String deletePowerUp(@PathVariable("id") Long id,
                                RedirectAttributes redirectAttributes) {
        try {
            powerUpService.deletePowerUp(id);
            redirectAttributes.addFlashAttribute("successMessage", "Power-up deleted successfully.");
        } catch (ResourceNotFoundException ex) {
            log.warn("Attempted to delete missing power-up {}", id, ex);
            redirectAttributes.addFlashAttribute("errorMessage", "Power-up no longer exists.");
        } catch (Exception ex) {
            log.error("Unexpected error deleting power-up {}", id, ex);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete power-up. Please try again.");
        }

        return AdminPage.POWERUPS.redirect();
    }
}
