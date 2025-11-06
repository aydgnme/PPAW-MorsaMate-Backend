package me.aydgn.MorseMate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.dto.response.UserResponse;
import me.aydgn.MorseMate.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/${api.version}/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;

    /**
     * Get all users (admin only)
     * GET /admin/users
     */
    @GetMapping("/users")
    public ResponseEntity<Page<UserResponse>> getAllUsers(Pageable pageable) {
        log.info("Admin request to get all users, page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());
        Page<UserResponse> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * Get user by ID (admin only)
     * GET /admin/users/{id}
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable("id") Long id) {
        log.info("Admin request to get user by ID: {}", id);
        UserResponse user = userService.getUserResponseById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Deactivate user account (admin only)
     * POST /admin/users/{id}/deactivate
     */
    @PostMapping("/users/{id}/deactivate")
    public ResponseEntity<MessageResponse> deactivateUser(@PathVariable("id") Long id) {
        log.info("Admin request to deactivate user: {}", id);
        userService.deactivateAccount(id);
        return ResponseEntity.ok(new MessageResponse("User deactivated successfully"));
    }

    /**
     * Activate user account (admin only)
     * POST /admin/users/{id}/activate
     */
    @PostMapping("/users/{id}/activate")
    public ResponseEntity<MessageResponse> activateUser(@PathVariable("id") Long id) {
        log.info("Admin request to activate user: {}", id);
        userService.activateAccount(id);
        return ResponseEntity.ok(new MessageResponse("User activated successfully"));
    }

    /**
     * Verify user email (admin only)
     * POST /admin/users/{id}/verify-email
     */
    @PostMapping("/users/{id}/verify-email")
    public ResponseEntity<MessageResponse> verifyUserEmail(@PathVariable("id") Long id) {
        log.info("Admin request to verify email for user: {}", id);
        userService.verifyEmail(id);
        return ResponseEntity.ok(new MessageResponse("Email verified successfully"));
    }

    record MessageResponse(String message) {}
}
