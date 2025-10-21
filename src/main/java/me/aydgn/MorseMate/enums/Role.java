package me.aydgn.MorseMate.enums;

/**
 * User role enumeration for access control.
 * Defines the authorization levels within the application.
 *
 * @author MorseMate Team
 * @version 1.0
 * @since 1.0
 */
public enum Role {
    /**
     * Regular user with standard access rights.
     * Can access learning features, manage their own profile, and view public content.
     */
    USER,

    /**
     * Administrator with elevated privileges.
     * Can manage categories, view all users, and access administrative endpoints.
     */
    ADMIN;

    /**
     * Returns the role name with ROLE_ prefix for Spring Security.
     * This is the standard format expected by Spring Security's role-based authorization.
     *
     * @return Role name with ROLE_ prefix (e.g., "ROLE_USER", "ROLE_ADMIN")
     */
    public String getAuthority() {
        return "ROLE_" + this.name();
    }

    /**
     * Check if this role is ADMIN.
     *
     * @return true if this role is ADMIN, false otherwise
     */
    public boolean isAdmin() {
        return this == ADMIN;
    }

    /**
     * Check if this role is USER.
     *
     * @return true if this role is USER, false otherwise
     */
    public boolean isUser() {
        return this == USER;
    }
}
