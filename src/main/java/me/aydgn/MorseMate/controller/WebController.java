package me.aydgn.MorseMate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Web Controller for serving HTML pages
 * Handles browser requests and returns HTML views
 */
@Controller
@Slf4j
public class WebController {

    /**
     * Home page - redirects to login
     */
    @GetMapping("/")
    public String home() {
        log.debug("GET / - Redirecting to login");
        return "redirect:/auth/login";
    }

    /**
     * Login page
     * GET /auth/login
     */
    @GetMapping("/auth/login")
    public String loginPage() {
        log.debug("GET /auth/login - Serving login page");
        return "login.html";
    }

    /**
     * User dashboard page
     * GET /dashboard/user
     */
    @GetMapping("/dashboard/user")
    public String userDashboard() {
        log.debug("GET /dashboard/user - Serving user dashboard");
        return "dashboard-user.html";
    }

    /**
     * Premium user dashboard page
     * GET /dashboard/premium
     */
    @GetMapping("/dashboard/premium")
    public String premiumDashboard() {
        log.debug("GET /dashboard/premium - Serving premium dashboard");
        return "dashboard-premium.html";
    }

    /**
     * Admin dashboard page
     * GET /dashboard/admin
     */
    @GetMapping("/dashboard/admin")
    public String adminDashboard() {
        log.debug("GET /dashboard/admin - Serving admin dashboard");
        return "dashboard-admin.html";
    }

    /**
     * Register page
     * GET /auth/register
     */
    @GetMapping("/auth/register")
    public String registerPage() {
        log.debug("GET /auth/register - Serving register page");
        return "register.html";
    }
}
