package me.aydgn.MorseMate.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC Configuration
 *
 * Registers interceptors for request handling
 *
 * @author MorseMate Team
 * @version 1.0
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final DashboardAccessInterceptor dashboardAccessInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Register dashboard access interceptor
        registry.addInterceptor(dashboardAccessInterceptor)
                .addPathPatterns("/dashboard/**")
                .order(1); // Execute before other interceptors

        // Note: /admin/** is already protected by JWT in SecurityConfig
        // This interceptor is specifically for /dashboard/** routes
    }
}
