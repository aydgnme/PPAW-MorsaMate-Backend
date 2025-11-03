package me.aydgn.MorseMate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.dto.request.CreateAchievementRequest;
import me.aydgn.MorseMate.dto.request.UpdateAchievementRequest;
import me.aydgn.MorseMate.dto.response.AchievementResponse;
import me.aydgn.MorseMate.dto.response.ApiMessage;
import me.aydgn.MorseMate.service.AchievementService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Achievement management
 * Provides endpoints for managing achievements and user achievements
 */
@RestController
@RequestMapping("/${api.version}/achievements")
@RequiredArgsConstructor
@Slf4j
public class AchievementController {

    private final AchievementService achievementService;

    /**
     * GET /v1/achievements
     * Get all achievements
     */
    @GetMapping
    public ResponseEntity<List<AchievementResponse>> getAllAchievements() {
        log.debug("GET /v1/achievements - Fetching all achievements");
        List<AchievementResponse> achievements = achievementService.getAllAchievements();
        return ResponseEntity.ok(achievements);
    }

    /**
     * GET /v1/achievements/paged
     * Get all achievements with pagination
     */
    @GetMapping("/paged")
    public ResponseEntity<Page<AchievementResponse>> getAllAchievementsPaged(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        log.debug("GET /v1/achievements/paged - Fetching paged achievements");
        Page<AchievementResponse> achievements = achievementService.getAllAchievementsPaged(pageable);
        return ResponseEntity.ok(achievements);
    }

    /**
     * GET /v1/achievements/{id}
     * Get achievement by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<AchievementResponse> getAchievementById(@PathVariable Long id) {
        log.debug("GET /v1/achievements/{} - Fetching achievement", id);
        AchievementResponse achievement = achievementService.getAchievementById(id);
        return ResponseEntity.ok(achievement);
    }

    /**
     * GET /v1/achievements/name/{name}
     * Get achievement by name
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<AchievementResponse> getAchievementByName(@PathVariable String name) {
        log.debug("GET /v1/achievements/name/{} - Fetching achievement", name);
        AchievementResponse achievement = achievementService.getAchievementByName(name);
        return ResponseEntity.ok(achievement);
    }

    /**
     * POST /v1/achievements
     * Create a new achievement (Admin only)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AchievementResponse> createAchievement(
            @Valid @RequestBody CreateAchievementRequest request) {
        log.info("POST /v1/achievements - Creating new achievement: {}", request.getName());
        AchievementResponse created = achievementService.createAchievement(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /v1/achievements/{id}
     * Update an existing achievement (Admin only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AchievementResponse> updateAchievement(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAchievementRequest request) {
        log.info("PUT /v1/achievements/{} - Updating achievement", id);
        AchievementResponse updated = achievementService.updateAchievement(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /v1/achievements/{id}
     * Delete an achievement (Admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiMessage> deleteAchievement(@PathVariable Long id) {
        log.info("DELETE /v1/achievements/{} - Deleting achievement", id);
        achievementService.deleteAchievement(id);
        return ResponseEntity.ok(new ApiMessage("Achievement deleted successfully"));
    }

    /**
     * GET /v1/achievements/count
     * Get total achievement count
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getTotalAchievementCount() {
        log.debug("GET /v1/achievements/count - Counting achievements");
        long count = achievementService.getTotalAchievementCount();
        return ResponseEntity.ok(Map.of("totalAchievements", count));
    }

    // ==================== User Achievement Endpoints ====================

    /**
     * GET /v1/achievements/me
     * Get authenticated user's achievements
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AchievementResponse>> getMyAchievements(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        log.debug("GET /v1/achievements/me - Fetching achievements for user id: {}", userId);
        List<AchievementResponse> achievements = achievementService.getUserAchievements(userId);
        return ResponseEntity.ok(achievements);
    }

    /**
     * GET /v1/achievements/me/paged
     * Get authenticated user's achievements with pagination
     */
    @GetMapping("/me/paged")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<AchievementResponse>> getMyAchievementsPaged(
            Authentication authentication,
            @PageableDefault(size = 20, sort = "earnedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Long userId = Long.parseLong(authentication.getName());
        log.debug("GET /v1/achievements/me/paged - Fetching paged achievements for user id: {}", userId);
        Page<AchievementResponse> achievements = achievementService.getUserAchievementsPaged(userId, pageable);
        return ResponseEntity.ok(achievements);
    }

    /**
     * GET /v1/achievements/me/count
     * Get authenticated user's achievement count
     */
    @GetMapping("/me/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Long>> getMyAchievementCount(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        log.debug("GET /v1/achievements/me/count - Counting achievements for user id: {}", userId);
        long count = achievementService.getUserAchievementCount(userId);
        return ResponseEntity.ok(Map.of("achievementCount", count));
    }

    /**
     * GET /v1/achievements/{id}/earned
     * Check if authenticated user has earned specific achievement
     */
    @GetMapping("/{id}/earned")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Boolean>> hasEarnedAchievement(
            Authentication authentication,
            @PathVariable Long id) {
        Long userId = Long.parseLong(authentication.getName());
        log.debug("GET /v1/achievements/{}/earned - Checking for user id: {}", id, userId);
        boolean earned = achievementService.hasUserEarnedAchievement(userId, id);
        return ResponseEntity.ok(Map.of("earned", earned));
    }

    /**
     * POST /v1/achievements/{id}/award/{userId}
     * Award achievement to a user (Admin only)
     */
    @PostMapping("/{achievementId}/award/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiMessage> awardAchievementToUser(
            @PathVariable Long achievementId,
            @PathVariable Long userId) {
        log.info("POST /v1/achievements/{}/award/{} - Awarding achievement", achievementId, userId);
        achievementService.awardAchievementToUser(userId, achievementId);
        return ResponseEntity.ok(new ApiMessage("Achievement awarded successfully"));
    }

    /**
     * POST /v1/achievements/me/check
     * Check and award achievements for authenticated user
     */
    @PostMapping("/me/check")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiMessage> checkMyAchievements(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        log.info("POST /v1/achievements/me/check - Checking achievements for user id: {}", userId);
        achievementService.checkAndAwardAchievements(userId);
        return ResponseEntity.ok(new ApiMessage("Achievements checked successfully"));
    }

    // ==================== Admin User Achievement Endpoints ====================

    /**
     * GET /v1/achievements/users/{userId}
     * Get specific user's achievements (Admin only)
     */
    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AchievementResponse>> getUserAchievements(@PathVariable Long userId) {
        log.debug("GET /v1/achievements/users/{} - Admin fetching user achievements", userId);
        List<AchievementResponse> achievements = achievementService.getUserAchievements(userId);
        return ResponseEntity.ok(achievements);
    }

    /**
     * GET /v1/achievements/users/{userId}/count
     * Get specific user's achievement count (Admin only)
     */
    @GetMapping("/users/{userId}/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> getUserAchievementCount(@PathVariable Long userId) {
        log.debug("GET /v1/achievements/users/{}/count - Admin counting user achievements", userId);
        long count = achievementService.getUserAchievementCount(userId);
        return ResponseEntity.ok(Map.of("achievementCount", count));
    }
}
