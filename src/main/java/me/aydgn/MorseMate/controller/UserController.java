package me.aydgn.MorseMate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.dto.request.UpdateProfileRequest;
import me.aydgn.MorseMate.dto.response.UserResponse;
import me.aydgn.MorseMate.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/${api.version}/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * Get current user profile
     * GET /users/me
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        Long userId = getCurrentUserId();
        UserResponse user = userService.getUserResponseById(userId);
        return ResponseEntity.ok(user);
    }

    /**
     * Get user by ID
     * GET /users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserResponseById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Get user by username
     * GET /users/username/{username}
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username) {
        var user = userService.getUserByUsername(username);
        return ResponseEntity.ok(UserResponse.from(user));
    }

    /**
     * Update current user profile
     * PUT /users/me
     */
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        Long userId = getCurrentUserId();
        UserResponse user = userService.updateProfile(userId, request);
        return ResponseEntity.ok(user);
    }

    /**
     * Get all users (paginated)
     * GET /users
     */
    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(Pageable pageable) {
        Page<UserResponse> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * Get current user statistics
     * GET /users/me/statistics
     */
    @GetMapping("/me/statistics")
    public ResponseEntity<UserService.UserStatistics> getUserStatistics() {
        Long userId = getCurrentUserId();
        UserService.UserStatistics stats = userService.getUserStatistics(userId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Use a heart
     * POST /users/me/hearts/use
     */
    @PostMapping("/me/hearts/use")
    public ResponseEntity<?> useHeart() {
        Long userId = getCurrentUserId();
        boolean success = userService.useHeart(userId);

        if (success) {
            return ResponseEntity.ok(new MessageResponse("Heart used successfully"));
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("No hearts available"));
        }
    }

    /**
     * Refill hearts
     * POST /users/me/hearts/refill
     */
    @PostMapping("/me/hearts/refill")
    public ResponseEntity<MessageResponse> refillHearts() {
        Long userId = getCurrentUserId();
        userService.refillHearts(userId);
        return ResponseEntity.ok(new MessageResponse("Hearts refilled successfully"));
    }

    /**
     * Add points to user (admin/system use)
     * POST /users/{id}/points
     */
    @PostMapping("/{id}/points")
    public ResponseEntity<MessageResponse> addPoints(
            @PathVariable Long id,
            @RequestParam int points) {
        userService.addPoints(id, points);
        return ResponseEntity.ok(new MessageResponse("Points added successfully"));
    }

    /**
     * Deactivate user account
     * POST /users/{id}/deactivate
     */
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<MessageResponse> deactivateAccount(@PathVariable Long id) {
        userService.deactivateAccount(id);
        return ResponseEntity.ok(new MessageResponse("Account deactivated successfully"));
    }

    /**
     * Activate user account
     * POST /users/{id}/activate
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<MessageResponse> activateAccount(@PathVariable Long id) {
        userService.activateAccount(id);
        return ResponseEntity.ok(new MessageResponse("Account activated successfully"));
    }

    /**
     * Verify user email
     * POST /users/{id}/verify-email
     */
    @PostMapping("/{id}/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(@PathVariable Long id) {
        userService.verifyEmail(id);
        return ResponseEntity.ok(new MessageResponse("Email verified successfully"));
    }

    /**
     * Delete current user account
     * DELETE /users/me
     */
    @DeleteMapping("/me")
    public ResponseEntity<MessageResponse> deleteAccount() {
        Long userId = getCurrentUserId();
        userService.deactivateAccount(userId);
        return ResponseEntity.ok(new MessageResponse("Account deleted successfully"));
    }

    // Helper method to get current user ID from security context
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }
        return Long.parseLong(authentication.getName());
    }

    // Simple message response record
    record MessageResponse(String message) {}
}
