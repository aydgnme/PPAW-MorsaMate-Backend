package me.aydgn.MorseMate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.dto.request.IncrementAttemptsRequest;
import me.aydgn.MorseMate.dto.request.MarkLessonCompletedRequest;
import me.aydgn.MorseMate.dto.response.ApiMessage;
import me.aydgn.MorseMate.dto.response.UserProgressResponse;
import me.aydgn.MorseMate.service.UserProgressService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for User Progress management
 * Provides endpoints for tracking and managing user learning progress
 */
@RestController
@RequestMapping("/v1/progress")
@RequiredArgsConstructor
@Slf4j
public class UserProgressController {

    private final UserProgressService userProgressService;

    /**
     * GET /v1/progress/me
     * Get all progress records for the authenticated user
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserProgressResponse>> getMyProgress(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        log.debug("GET /v1/progress/me - Fetching progress for user id: {}", userId);

        List<UserProgressResponse> progress = userProgressService.getAllUserProgress(userId);
        return ResponseEntity.ok(progress);
    }

    /**
     * GET /v1/progress/me/paged
     * Get paginated progress records for the authenticated user
     */
    @GetMapping("/me/paged")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<UserProgressResponse>> getMyProgressPaged(
            Authentication authentication,
            @PageableDefault(size = 20, sort = "completedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Long userId = Long.parseLong(authentication.getName());
        log.debug("GET /v1/progress/me/paged - Fetching paged progress for user id: {}", userId);

        Page<UserProgressResponse> progress = userProgressService.getAllUserProgressPaged(userId, pageable);
        return ResponseEntity.ok(progress);
    }

    /**
     * GET /v1/progress/me/lessons/{lessonId}
     * Get progress for a specific lesson for the authenticated user
     */
    @GetMapping("/me/lessons/{lessonId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProgressResponse> getMyLessonProgress(
            Authentication authentication,
            @PathVariable Long lessonId) {
        Long userId = Long.parseLong(authentication.getName());
        log.debug("GET /v1/progress/me/lessons/{} - Fetching progress for user id: {}", lessonId, userId);

        UserProgressResponse progress = userProgressService.getUserProgressForLesson(userId, lessonId);
        return ResponseEntity.ok(progress);
    }

    /**
     * GET /v1/progress/me/statistics
     * Get overall statistics for the authenticated user
     */
    @GetMapping("/me/statistics")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getMyStatistics(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        log.debug("GET /v1/progress/me/statistics - Fetching statistics for user id: {}", userId);

        Map<String, Object> stats = userProgressService.getUserStatistics(userId);
        return ResponseEntity.ok(stats);
    }

    /**
     * POST /v1/progress/me/lessons/{lessonId}/increment
     * Increment attempt count for a lesson
     */
    @PostMapping("/me/lessons/{lessonId}/increment")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProgressResponse> incrementAttempts(
            Authentication authentication,
            @PathVariable Long lessonId) {
        Long userId = Long.parseLong(authentication.getName());
        log.info("POST /v1/progress/me/lessons/{}/increment - Incrementing attempts for user id: {}", lessonId, userId);

        UserProgressResponse progress = userProgressService.incrementAttempts(userId, lessonId);
        return ResponseEntity.ok(progress);
    }

    /**
     * POST /v1/progress/me/lessons/{lessonId}/complete
     * Mark a lesson as completed
     */
    @PostMapping("/me/lessons/{lessonId}/complete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProgressResponse> markLessonCompleted(
            Authentication authentication,
            @PathVariable Long lessonId,
            @Valid @RequestBody MarkLessonCompletedRequest request) {
        Long userId = Long.parseLong(authentication.getName());
        log.info("POST /v1/progress/me/lessons/{}/complete - Marking completed for user id: {}", lessonId, userId);

        UserProgressResponse progress = userProgressService.markLessonCompleted(
                userId, lessonId, request.getScore(), request.getStarsEarned(), request.getTimeSpent()
        );
        return ResponseEntity.ok(progress);
    }

    /**
     * POST /v1/progress/me/lessons/{lessonId}/reset
     * Reset lesson completion (for retrying)
     */
    @PostMapping("/me/lessons/{lessonId}/reset")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProgressResponse> resetLessonCompletion(
            Authentication authentication,
            @PathVariable Long lessonId) {
        Long userId = Long.parseLong(authentication.getName());
        log.info("POST /v1/progress/me/lessons/{}/reset - Resetting completion for user id: {}", lessonId, userId);

        UserProgressResponse progress = userProgressService.resetLessonCompletion(userId, lessonId);
        return ResponseEntity.ok(progress);
    }

    /**
     * GET /v1/progress/me/lessons/{lessonId}/completed
     * Check if user has completed a specific lesson
     */
    @GetMapping("/me/lessons/{lessonId}/completed")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Boolean>> hasCompletedLesson(
            Authentication authentication,
            @PathVariable Long lessonId) {
        Long userId = Long.parseLong(authentication.getName());
        log.debug("GET /v1/progress/me/lessons/{}/completed - Checking completion for user id: {}", lessonId, userId);

        boolean completed = userProgressService.hasCompletedLesson(userId, lessonId);
        return ResponseEntity.ok(Map.of("completed", completed));
    }

    /**
     * GET /v1/progress/me/completed-count
     * Get count of completed lessons for the authenticated user
     */
    @GetMapping("/me/completed-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Long>> getCompletedLessonCount(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        log.debug("GET /v1/progress/me/completed-count - Counting completed lessons for user id: {}", userId);

        long count = userProgressService.getCompletedLessonCount(userId);
        return ResponseEntity.ok(Map.of("completedLessons", count));
    }

    // ==================== Admin Endpoints ====================

    /**
     * GET /v1/progress/users/{userId}
     * Get all progress for a specific user (Admin only)
     */
    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserProgressResponse>> getUserProgress(@PathVariable Long userId) {
        log.debug("GET /v1/progress/users/{} - Admin fetching progress", userId);

        List<UserProgressResponse> progress = userProgressService.getAllUserProgress(userId);
        return ResponseEntity.ok(progress);
    }

    /**
     * GET /v1/progress/lessons/{lessonId}
     * Get progress for a specific lesson (all users) - Admin only
     */
    @GetMapping("/lessons/{lessonId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserProgressResponse>> getLessonProgress(@PathVariable Long lessonId) {
        log.debug("GET /v1/progress/lessons/{} - Admin fetching lesson progress", lessonId);

        List<UserProgressResponse> progress = userProgressService.getLessonProgress(lessonId);
        return ResponseEntity.ok(progress);
    }

    /**
     * GET /v1/progress/lessons/{lessonId}/statistics
     * Get statistics for a lesson (all users) - Admin only
     */
    @GetMapping("/lessons/{lessonId}/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getLessonStatistics(@PathVariable Long lessonId) {
        log.debug("GET /v1/progress/lessons/{}/statistics - Admin fetching lesson statistics", lessonId);

        Map<String, Object> stats = userProgressService.getLessonStatistics(lessonId);
        return ResponseEntity.ok(stats);
    }
}
