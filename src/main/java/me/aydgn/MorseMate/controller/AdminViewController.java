package me.aydgn.MorseMate.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Admin Panel MVC Controller
 *
 * Handles web UI requests for admin panel pages.
 * Uses MVC pattern with static HTML views.
 */
@Controller
@RequestMapping("/admin")
public class AdminViewController {

    /**
     * GET /admin/login
     * Display admin login page
     */
    @GetMapping("/login")
    public String loginPage() {
        return "forward:/admin/login.html";
    }

    /**
     * GET /admin/dashboard
     * Display admin dashboard (requires authentication)
     */
    @GetMapping("/dashboard")
    public String dashboardPage() {
        return "forward:/admin/dashboard.html";
    }

    /**
     * GET /admin (redirect to dashboard)
     */
    @GetMapping
    public String adminRoot() {
        return "redirect:/admin/dashboard";
    }
}
