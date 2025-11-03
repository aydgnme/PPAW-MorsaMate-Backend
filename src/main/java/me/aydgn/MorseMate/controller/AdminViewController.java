package me.aydgn.MorseMate.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Info Panel MVC Controller
 *
 * Handles web UI requests for info panel pages (formerly admin panel).
 * Uses MVC pattern with static HTML views.
 *
 * Note: This handles old /admin routes for backward compatibility.
 * New applications should use /info-panel routes directly.
 */
@Controller
@RequestMapping("/admin")
public class AdminViewController {

    /**
     * GET /admin/login - Redirect to new location
     */
    @GetMapping("/login")
    public String loginPage() {
        return "redirect:/info-panel/login.html";
    }

    /**
     * GET /admin/dashboard - Redirect to new location
     */
    @GetMapping("/dashboard")
    public String dashboardPage() {
        return "redirect:/info-panel/dashboard.html";
    }

    /**
     * GET /admin - Redirect to new location
     */
    @GetMapping
    public String adminRoot() {
        return "redirect:/info-panel";
    }
}
