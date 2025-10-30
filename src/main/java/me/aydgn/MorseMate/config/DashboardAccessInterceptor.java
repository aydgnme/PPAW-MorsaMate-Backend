package me.aydgn.MorseMate.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Dashboard Access Interceptor
 *
 * Security interceptor for /dashboard/* routes
 * Ensures only authenticated admin users can access dashboard
 *
 * Flow:
 * - Not authenticated → redirect to aydgn.me
 * - Authenticated but not ADMIN → redirect to aydgn.me
 * - Authenticated as ADMIN → allow access
 *
 * @author MorseMate Team
 * @version 1.0
 */
@Component
@Slf4j
public class DashboardAccessInterceptor implements HandlerInterceptor {

    @Value("${app.redirect-url:https://aydgn.me}")
    private String redirectUrl;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestUri = request.getRequestURI();
        log.debug("Dashboard access attempt: {} from IP: {}", requestUri, request.getRemoteAddr());

        // Get authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Check if authenticated
        boolean isAuthenticated = authentication != null &&
                                authentication.isAuthenticated() &&
                                !"anonymousUser".equals(authentication.getPrincipal());

        if (!isAuthenticated) {
            log.warn("Unauthenticated access attempt to dashboard: {}", requestUri);
            response.sendRedirect(redirectUrl);
            return false;
        }

        // Check if ADMIN
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> "ROLE_ADMIN".equals(auth));

        if (!isAdmin) {
            log.warn("Non-admin user {} attempted to access dashboard: {}",
                    authentication.getName(), requestUri);
            response.sendRedirect(redirectUrl);
            return false;
        }

        // Admin user - allow access
        log.info("Admin user {} accessing dashboard: {}", authentication.getName(), requestUri);
        return true;
    }
}
