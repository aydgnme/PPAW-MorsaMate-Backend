package me.aydgn.MorseMate.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * Info Panel Controller - Smart routing for admin access
 *
 * Security Flow:
 * - Not authenticated → redirect to aydgn.me
 * - Authenticated as USER → redirect to aydgn.me
 * - Authenticated as ADMIN → redirect to /info-panel/dashboard.html
 *
 * @author MorseMate Team
 * @version 1.0
 */
@RestController
@RequestMapping("/info-panel")
@Slf4j
public class InfoPanelController {

    @Value("${app.redirect-url:https://aydgn.me}")
    private String redirectUrl;

    @Value("${app.admin-panel-url:/info-panel/dashboard.html}")
    private String adminPanelUrl;

    /**
     * GET /info-panel
     * Smart routing based on authentication status
     *
     * Flow:
     * 1. Check if user is authenticated
     * 2. If not → redirect to aydgn.me
     * 3. If authenticated, check role
     * 4. If ADMIN → redirect to admin panel
     * 5. If USER → redirect to aydgn.me (security measure)
     *
     * @param request HTTP request
     * @return Redirect to appropriate location
     */
    @GetMapping
    public ResponseEntity<Void> infoPanel(HttpServletRequest request) {
        log.info("Info panel accessed from IP: {}", request.getRemoteAddr());

        // Get authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Check if authenticated
        boolean isAuthenticated = authentication != null &&
                                authentication.isAuthenticated() &&
                                !"anonymousUser".equals(authentication.getPrincipal());

        if (!isAuthenticated) {
            log.debug("User not authenticated, redirecting to: {}", redirectUrl);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(redirectUrl))
                    .build();
        }

        // Check if ADMIN
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> "ROLE_ADMIN".equals(auth));

        if (isAdmin) {
            log.info("Admin user accessing info panel, redirecting to admin dashboard");
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(adminPanelUrl))
                    .build();
        }

        // Regular user - redirect to main site for security
        log.debug("Regular user accessing info panel, redirecting to: {}", redirectUrl);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(redirectUrl))
                .build();
    }
}
