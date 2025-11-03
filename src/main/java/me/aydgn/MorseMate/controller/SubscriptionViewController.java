package me.aydgn.MorseMate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.dto.response.UserSubscriptionResponse;
import me.aydgn.MorseMate.service.UserSubscriptionService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * MVC Controller for Subscription Views (Lab 6).
 * Handles rendering Thymeleaf templates for subscription management.
 *
 * This controller implements the VIEW layer of MVC pattern:
 * - Returns view names instead of JSON responses
 * - Uses Thymeleaf templates for server-side rendering
 * - Provides data to views via Model objects
 */
@Controller
@RequestMapping("/subscriptions")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionViewController {

    private final UserSubscriptionService subscriptionService;

    /**
     * Display subscription list/management page.
     * Shows current subscription if exists, or available plans otherwise.
     *
     * URL: GET /subscriptions/list
     * Template: subscriptions/list.html
     *
     * @param model Spring Model for passing data to view
     * @return View name "subscriptions/list"
     */
    @GetMapping("/list")
    public String subscriptionList(Model model) {
        try {
            Long userId = getCurrentUserId();
            log.info("Loading subscription list page for user ID: {}", userId);

            // Get user's current subscription
            UserSubscriptionResponse subscription = subscriptionService.getUserSubscription(userId);

            // Add data to model for Thymeleaf
            model.addAttribute("subscription", subscription);
            model.addAttribute("userId", userId);

            log.debug("Subscription data added to model: {}", subscription != null ? "Active" : "None");

            return "subscriptions/list";
        } catch (Exception e) {
            log.error("Error loading subscription list", e);
            model.addAttribute("error", "Unable to load subscription data");
            return "error"; // Fallback error page
        }
    }

    /**
     * Display subscription history page.
     * Shows timeline of all user subscriptions (past and present).
     *
     * URL: GET /subscriptions/history
     * Template: subscriptions/history.html
     *
     * @param model Spring Model for passing data to view
     * @return View name "subscriptions/history"
     */
    @GetMapping("/history")
    public String subscriptionHistory(Model model) {
        try {
            Long userId = getCurrentUserId();
            log.info("Loading subscription history page for user ID: {}", userId);

            // Get subscription history
            List<UserSubscriptionResponse> history = subscriptionService.getSubscriptionHistory(userId);

            // Add data to model
            model.addAttribute("history", history);
            model.addAttribute("userId", userId);

            log.debug("History loaded: {} subscriptions", history.size());

            return "subscriptions/history";
        } catch (Exception e) {
            log.error("Error loading subscription history", e);
            model.addAttribute("error", "Unable to load subscription history");
            return "error";
        }
    }

    /**
     * Display subscription upgrade page.
     * Shows available plans with comparison table.
     *
     * URL: GET /subscriptions/upgrade
     * Template: subscriptions/upgrade.html
     *
     * @param model Spring Model for passing data to view
     * @return View name "subscriptions/upgrade"
     */
    @GetMapping("/upgrade")
    public String subscriptionUpgrade(Model model) {
        try {
            Long userId = getCurrentUserId();
            log.info("Loading subscription upgrade page for user ID: {}", userId);

            // Get current subscription for comparison
            UserSubscriptionResponse currentSubscription = subscriptionService.getUserSubscription(userId);

            // Add data to model
            model.addAttribute("currentSubscription", currentSubscription);
            model.addAttribute("userId", userId);

            log.debug("Upgrade page loaded with current subscription: {}",
                    currentSubscription != null ? currentSubscription.getPlan().getName() : "None");

            return "subscriptions/upgrade";
        } catch (Exception e) {
            log.error("Error loading subscription upgrade page", e);
            model.addAttribute("error", "Unable to load upgrade options");
            return "error";
        }
    }

    /**
     * Default redirect - redirect to list page.
     *
     * URL: GET /subscriptions or /subscriptions/
     *
     * @return Redirect to /subscriptions/list
     */
    @GetMapping({"", "/"})
    public String index() {
        log.debug("Redirecting from /subscriptions to /subscriptions/list");
        return "redirect:/subscriptions/list";
    }

    /**
     * Helper method to get current authenticated user ID.
     * Extracts user ID from Spring Security context.
     *
     * @return Current user ID
     * @throws IllegalStateException if user is not authenticated
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("User not authenticated when accessing subscription views");
            throw new IllegalStateException("User not authenticated");
        }

        String username = authentication.getName();
        log.trace("Authenticated user: {}", username);

        return Long.parseLong(username);
    }
}
