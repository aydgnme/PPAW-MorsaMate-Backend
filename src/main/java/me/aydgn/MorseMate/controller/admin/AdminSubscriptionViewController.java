package me.aydgn.MorseMate.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.dto.response.SubscriptionPlanResponse;
import me.aydgn.MorseMate.service.SubscriptionPlanService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin controller for subscription plan viewing.
 * View-only implementation for now.
 */
@Controller
@RequestMapping("/admin/subscriptions")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminSubscriptionViewController extends AbstractAdminPageController {

    private final SubscriptionPlanService subscriptionPlanService;

    @GetMapping
    public String listSubscriptionPlans(Model model) {
        log.debug("Admin viewing subscription plans");

        List<SubscriptionPlanResponse> plans = subscriptionPlanService.getAllPlans();
        model.addAttribute("plans", plans);

        return render(model, AdminPage.SUBSCRIPTIONS);
    }
}
