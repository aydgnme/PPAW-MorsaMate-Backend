package me.aydgn.MorseMate.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.dto.request.LoginRequest;
import me.aydgn.MorseMate.dto.response.AuthResponse;
import me.aydgn.MorseMate.dto.response.UserResponse;
import me.aydgn.MorseMate.service.AuthService;
import me.aydgn.MorseMate.service.UserService;
import me.aydgn.MorseMate.service.admin.AdminDashboardService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.time.Duration;

/**
 * Admin Web Controller
 *
 * Serves Thymeleaf templates for administrator tooling.
 * Complements the REST-based admin endpoints by providing
 * a classic web dashboard experience.
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminViewController extends AbstractAdminPageController {

    private final UserService userService;
    private final AuthService authService;
    private final AdminDashboardService adminDashboardService;

    @Value("${api.version}")
    private String apiVersion;

    /**
     * Admin login page (public).
     */
    @GetMapping("/login")
    public String loginPage() {
        return "admin/login";
    }

    /**
     * Handle admin login form submission.
     */
    @PostMapping("/login")
    public String handleLogin(@RequestParam("identifier") String identifier,
                              @RequestParam("password") String password,
                              @RequestParam(value = "rememberMe", defaultValue = "false") boolean rememberMe,
                              RedirectAttributes redirectAttributes,
                              HttpServletResponse response) {
        String trimmedIdentifier = identifier != null ? identifier.trim() : "";

        try {
            AuthResponse authResponse = authService.login(
                    LoginRequest.builder()
                            .identifier(trimmedIdentifier)
                            .password(password)
                            .rememberMe(rememberMe)
                            .build()
            );

            String role = authResponse.getUser() != null ? authResponse.getUser().getRole() : null;
            if (role == null || !"ADMIN".equalsIgnoreCase(role)) {
                redirectAttributes.addFlashAttribute("loginError", "You do not have administrator privileges.");
                redirectAttributes.addFlashAttribute("identifier", trimmedIdentifier);
                return "redirect:/admin/login";
            }

            long cookieSeconds = authResponse.getExpiresIn() != null
                    ? authResponse.getExpiresIn()
                    : 60L * 60L; // fallback 1 hour

            if (rememberMe) {
                cookieSeconds = Math.max(cookieSeconds, 60L * 60L * 24L * 7L); // at least 7 days
            }

            ResponseCookie jwtCookie = ResponseCookie.from("ADMIN_TOKEN", authResponse.getAccessToken())
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(Duration.ofSeconds(cookieSeconds))
                    .sameSite("Lax")
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());

            return "redirect:/admin";
        } catch (BadCredentialsException ex) {
            log.warn("Admin login failed for identifier {}", identifier);
            redirectAttributes.addFlashAttribute("loginError", ex.getMessage());
        } catch (Exception ex) {
            log.error("Unexpected error during admin login", ex);
            redirectAttributes.addFlashAttribute("loginError", "Unable to sign in. Please try again.");
        }

        redirectAttributes.addFlashAttribute("identifier", trimmedIdentifier);
        return "redirect:/admin/login";
    }

    /**
     * Clear admin session cookie.
     */
    @GetMapping("/logout")
    public String logout(HttpServletResponse response, RedirectAttributes redirectAttributes) {
        ResponseCookie clearCookie = ResponseCookie.from("ADMIN_TOKEN", "")
                .path("/")
                .maxAge(Duration.ZERO)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, clearCookie.toString());
        redirectAttributes.addFlashAttribute("logoutMessage", "You have been signed out.");
        return "redirect:/admin/login";
    }

    /**
     * Admin-facing user management view.
     * GET /admin/users
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public String usersPage(@RequestParam(value = "page", defaultValue = "0") int page,
                            @RequestParam(value = "size", defaultValue = "20") int size,
                            Model model) {
        int pageNumber = Math.max(page, 0);
        int pageSize = Math.min(Math.max(size, 1), 50);

        try {
            Page<UserResponse> usersPage = userService.getAllUsers(
                    PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.ASC, "id"))
            );

            model.addAttribute("usersPage", usersPage);
            model.addAttribute("users", usersPage.getContent());
            model.addAttribute("totalPages", usersPage.getTotalPages());
        } catch (Exception ex) {
            log.error("Failed to load admin user list (page={}, size={})", pageNumber, pageSize, ex);
            model.addAttribute("usersPage", null);
            model.addAttribute("users", Collections.emptyList());
            model.addAttribute("totalPages", 0);
            model.addAttribute("loadError", "Unable to load user data right now. Please try again later.");
        }

        model.addAttribute("currentPage", pageNumber);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("apiVersion", apiVersion);
        return render(model, AdminPage.USERS);
    }

    /**
     * GET /admin - Admin dashboard landing page.
     * Displays quick links and summary panels.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String adminRoot(Model model) {
        // TODO: Fetch real statistics
        // For now, provide placeholder stats
        model.addAttribute("stats", adminDashboardService.getDashboardStats());
        model.addAttribute("recentActivity", adminDashboardService.getRecentActivity());
        return render(model, AdminPage.DASHBOARD);
    }
}
