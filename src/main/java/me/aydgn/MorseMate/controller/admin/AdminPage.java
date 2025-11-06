package me.aydgn.MorseMate.controller.admin;

import java.util.Objects;

/**
 * Canonical list of admin pages served by Thymeleaf views.
 * Each enum constant encapsulates the navigation key, Thymeleaf view name,
 * and the HTTP path used for redirect helpers.
 */
public enum AdminPage {
    DASHBOARD("dashboard", "admin/index", "/admin"),
    CATEGORIES("categories", "admin/categories", "/admin/categories"),
    LESSONS("lessons", "admin/lessons", "/admin/lessons"),
    EXERCISES("exercises", "admin/exercises", "/admin/exercises"),
    USERS("users", "admin/users", "/admin/users"),
    SUBSCRIPTIONS("subscriptions", "admin/subscriptions", "/admin/subscriptions"),
    ACHIEVEMENTS("achievements", "admin/achievements", "/admin/achievements"),
    GEMS("gems", "admin/gems", "/admin/gems"),
    POWERUPS("powerups", "admin/powerups", "/admin/powerups"),
    LEADERBOARD("leaderboard", "admin/leaderboard", "/admin/leaderboard");

    private final String navigationKey;
    private final String viewName;
    private final String path;

    AdminPage(String navigationKey, String viewName, String path) {
        this.navigationKey = Objects.requireNonNull(navigationKey, "navigationKey must not be null");
        this.viewName = Objects.requireNonNull(viewName, "viewName must not be null");
        this.path = Objects.requireNonNull(path, "path must not be null");
    }

    public String getNavigationKey() {
        return navigationKey;
    }

    public String getViewName() {
        return viewName;
    }

    public String getPath() {
        return path;
    }

    /**
     * Convenience helper for Spring MVC redirect prefixes.
     */
    public String redirect() {
        return "redirect:" + path;
    }
}
