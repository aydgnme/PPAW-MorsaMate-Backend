package me.aydgn.MorseMate.config;

import lombok.RequiredArgsConstructor;
import me.aydgn.MorseMate.security.AdminAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AdminAuthenticationEntryPoint adminAuthenticationEntryPoint;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/", "/api").permitAll()
                        .requestMatchers("/api/health", "/api/ping", "/api/info").permitAll()
                        .requestMatchers("/v1/auth/**").permitAll()
                        .requestMatchers("/v1/categories", "/v1/categories/**").permitAll()
                        .requestMatchers("/v1/lessons", "/v1/lessons/**").permitAll()
                        .requestMatchers("/api-docs/**").permitAll()
                        .requestMatchers("/api/openapi.json").permitAll()

                        // Info Panel - Smart routing endpoint (handles auth internally)
                        .requestMatchers("/info-panel").permitAll()

                        // Info Panel Pages (MVC pages - publicly accessible, protected by JWT in frontend)
                        .requestMatchers("/info-panel/**").permitAll()

                        // Admin auth endpoints (public)
                        .requestMatchers("/admin/login", "/admin/logout").permitAll()

                        // Admin Panel MVC pages
                        .requestMatchers("/admin", "/admin/**").hasRole("ADMIN")

                        // Static resources (CSS, JS, HTML pages)
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/fonts/**", "/img/**", "/pages/**").permitAll()
                        .requestMatchers("/*.html", "/*.ico", "/*.png", "/*.jpg").permitAll()

                        // Dashboard pages (protected by DashboardAccessInterceptor)
                        // Allows Spring Security to pass through, interceptor handles the redirect
                        .requestMatchers("/dashboard/**").permitAll()

                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .defaultAuthenticationEntryPointFor(adminAuthenticationEntryPoint, new AntPathRequestMatcher("/admin/**"))
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
