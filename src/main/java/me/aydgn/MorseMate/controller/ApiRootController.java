package me.aydgn.MorseMate.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.dto.response.ApiRootResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * REST Controller for API root endpoint.
 * Provides API overview and available endpoints for discovery.
 *
 * @author MorseMate Team
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping
@Slf4j
public class ApiRootController {

    @Value("${app.version:1.0.0}")
    private String version;

    @Value("${app.name:MorseMate}")
    private String appName;

    @Value("${app.redirect-url:https://aydgn.me}")
    private String redirectUrl;

    private static final String WELCOME_MESSAGE = "Welcome to %s API - Morse Code Learning Platform";

    /**
     * GET /
     * Returns API root information with available endpoints.
     * This endpoint serves as the main entry point for API discovery.
     * Redirects to aydgn.me if no valid Bearer token is provided.
     *
     * @param request HTTP request to extract authorization header
     * @return ResponseEntity containing API overview and available endpoints, or redirect
     */
    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getRoot(HttpServletRequest request) {
        log.debug("API root endpoint accessed");

        // Check for valid authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null &&
                                authentication.isAuthenticated() &&
                                !"anonymousUser".equals(authentication.getPrincipal());

        // If not authenticated, redirect to aydgn.me
        if (!isAuthenticated) {
            log.debug("No valid authentication found, redirecting to: {}", redirectUrl);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(redirectUrl))
                    .build();
        }

        // Check user role
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> "ROLE_ADMIN".equals(auth));

        log.debug("Authenticated user accessing root endpoint. Admin: {}", isAdmin);

        ApiRootResponse response = buildApiRootResponse(isAdmin);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api
     * Alternative API root endpoint.
     * Returns the same information as the root endpoint.
     *
     * @param request HTTP request to extract authorization header
     * @return ResponseEntity containing API overview and available endpoints, or redirect
     */
    @GetMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getApiRoot(HttpServletRequest request) {
        log.debug("API /api endpoint accessed - delegating to root");
        return getRoot(request);
    }

    /**
     * Builds the API root response with all available endpoints.
     *
     * @param isAdmin whether the current user has admin role
     * @return ApiRootResponse containing API metadata and endpoint map
     */
    private ApiRootResponse buildApiRootResponse(boolean isAdmin) {
        Map<String, String> endpoints = buildEndpointsMap(isAdmin);

        return ApiRootResponse.builder()
                .message(String.format(WELCOME_MESSAGE, appName))
                .version(version)
                .endpoints(endpoints)
                .build();
    }

    /**
     * Creates a map of all available API endpoints.
     * Endpoints are organized by category for better discoverability.
     * Admin-only endpoints are only included if the user has admin role.
     *
     * @param isAdmin whether the current user has admin role
     * @return LinkedHashMap preserving insertion order of endpoints
     */
    private Map<String, String> buildEndpointsMap(boolean isAdmin) {
        Map<String, String> endpoints = new LinkedHashMap<>();

        // System & Health endpoints (Public)
        endpoints.put("health", "/api/health");
        endpoints.put("ping", "/api/ping");
        endpoints.put("system-info", "/api/info");

        // Authentication endpoints (Public)
        endpoints.put("auth-register", "/auth/register");
        endpoints.put("auth-login", "/auth/login");
        endpoints.put("auth-me", "/auth/me");
        endpoints.put("auth-health", "/auth/health");

        // User Management endpoints (Authenticated)
        endpoints.put("users-list", "/users");
        endpoints.put("user-profile", "/users/me");
        endpoints.put("user-by-id", "/users/{id}");
        endpoints.put("user-by-username", "/users/username/{username}");
        endpoints.put("user-statistics", "/users/me/statistics");
        endpoints.put("user-hearts-use", "/users/me/hearts/use");
        endpoints.put("user-hearts-refill", "/users/me/hearts/refill");

        // Admin-only User Management endpoints
        if (isAdmin) {
            endpoints.put("user-add-points", "/users/{id}/points [ADMIN]");
            endpoints.put("user-deactivate", "/users/{id}/deactivate [ADMIN]");
            endpoints.put("user-activate", "/users/{id}/activate [ADMIN]");
            endpoints.put("user-verify-email", "/users/{id}/verify-email [ADMIN]");
        }

        // Category Management endpoints (Public read)
        endpoints.put("categories-list", "/v1/categories");
        endpoints.put("category-by-id", "/v1/categories/{id}");

        // Admin-only Category Management endpoints
        if (isAdmin) {
            endpoints.put("category-create", "/v1/categories [ADMIN]");
            endpoints.put("category-update", "/v1/categories/{id} [ADMIN]");
            endpoints.put("category-delete", "/v1/categories/{id} [ADMIN]");
            endpoints.put("categories-admin-all", "/v1/categories/admin/all [ADMIN]");
        }

        // API Documentation (Public)
        endpoints.put("api-docs-json", "/api-docs (Accept: application/json)");
        endpoints.put("api-docs-ui", "/api-docs (Accept: text/html)");

        return endpoints;
    }
}
