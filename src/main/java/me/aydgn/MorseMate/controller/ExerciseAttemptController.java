package me.aydgn.MorseMate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.dto.request.CreateExerciseAttemptRequest;
import me.aydgn.MorseMate.dto.response.ExerciseAttemptResponse;
import me.aydgn.MorseMate.service.ExerciseAttemptService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Exercise Attempt management
 * Provides endpoints for recording and tracking exercise attempts
 */
@RestController
@RequestMapping("/${api.version}/attempts")
@RequiredArgsConstructor
@Slf4j
public class ExerciseAttemptController {

    private final ExerciseAttemptService exerciseAttemptService;

    /**
     * POST /v1/attempts
     * Record a new exercise attempt for the authenticated user
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ExerciseAttemptResponse> recordAttempt(
            Authentication authentication,
            @Valid @RequestBody CreateExerciseAttemptRequest request) {
        Long userId = Long.parseLong(authentication.getName());
        log.info("POST /v1/attempts - Recording attempt for user id: {} on exercise id: {}",
                userId, request.getExerciseId());

        ExerciseAttemptResponse attempt = exerciseAttemptService.recordAttempt(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(attempt);
    }

    /**
     * GET /v1/attempts/me
     * Get all attempts for the authenticated user
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ExerciseAttemptResponse>> getMyAttempts(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        log.debug("GET /v1/attempts/me - Fetching attempts for user id: {}", userId);

        List<ExerciseAttemptResponse> attempts = exerciseAttemptService.getUserAttempts(userId);
        return ResponseEntity.ok(attempts);
    }

    /**
     * GET /v1/attempts/me/paged
     * Get paginated attempts for the authenticated user
     */
    @GetMapping("/me/paged")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<ExerciseAttemptResponse>> getMyAttemptsPaged(
            Authentication authentication,
            @PageableDefault(size = 20, sort = "attemptedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Long userId = Long.parseLong(authentication.getName());
        log.debug("GET /v1/attempts/me/paged - Fetching paged attempts for user id: {}", userId);

        Page<ExerciseAttemptResponse> attempts = exerciseAttemptService.getUserAttemptsPaged(userId, pageable);
        return ResponseEntity.ok(attempts);
    }

    /**
     * GET /v1/attempts/me/exercises/{exerciseId}
     * Get attempt history for a specific exercise
     */
    @GetMapping("/me/exercises/{exerciseId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ExerciseAttemptResponse>> getMyExerciseHistory(
            Authentication authentication,
            @PathVariable("exerciseId") Long exerciseId) {
        Long userId = Long.parseLong(authentication.getName());
        log.debug("GET /v1/attempts/me/exercises/{} - Fetching history for user id: {}", exerciseId, userId);

        List<ExerciseAttemptResponse> attempts = exerciseAttemptService.getUserExerciseHistory(userId, exerciseId);
        return ResponseEntity.ok(attempts);
    }

    /**
     * GET /v1/attempts/me/exercises/{exerciseId}/last
     * Get the last attempt for a specific exercise
     */
    @GetMapping("/me/exercises/{exerciseId}/last")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ExerciseAttemptResponse> getMyLastAttempt(
            Authentication authentication,
            @PathVariable("exerciseId") Long exerciseId) {
        Long userId = Long.parseLong(authentication.getName());
        log.debug("GET /v1/attempts/me/exercises/{}/last - Fetching last attempt for user id: {}", exerciseId, userId);

        ExerciseAttemptResponse attempt = exerciseAttemptService.getLastAttempt(userId, exerciseId);
        return ResponseEntity.ok(attempt);
    }

    /**
     * GET /v1/attempts/me/exercises/{exerciseId}/statistics
     * Get statistics for a specific exercise
     */
    @GetMapping("/me/exercises/{exerciseId}/statistics")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getMyExerciseStatistics(
            Authentication authentication,
            @PathVariable("exerciseId") Long exerciseId) {
        Long userId = Long.parseLong(authentication.getName());
        log.debug("GET /v1/attempts/me/exercises/{}/statistics - Fetching stats for user id: {}", exerciseId, userId);

        Map<String, Object> stats = exerciseAttemptService.getUserExerciseStatistics(userId, exerciseId);
        return ResponseEntity.ok(stats);
    }

    /**
     * GET /v1/attempts/me/statistics
     * Get overall attempt statistics for the authenticated user
     */
    @GetMapping("/me/statistics")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getMyStatistics(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        log.debug("GET /v1/attempts/me/statistics - Fetching overall stats for user id: {}", userId);

        Map<String, Object> stats = exerciseAttemptService.getUserStatistics(userId);
        return ResponseEntity.ok(stats);
    }

    /**
     * GET /v1/attempts/me/range?from={from}&to={to}
     * Get attempts within a time range
     */
    @GetMapping("/me/range")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ExerciseAttemptResponse>> getMyAttemptsInRange(
            Authentication authentication,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        Long userId = Long.parseLong(authentication.getName());
        log.debug("GET /v1/attempts/me/range - Fetching attempts for user id: {} from {} to {}", userId, from, to);

        List<ExerciseAttemptResponse> attempts = exerciseAttemptService.getUserAttemptsInRange(userId, from, to);
        return ResponseEntity.ok(attempts);
    }

    /**
     * GET /v1/attempts/me/correctness-rate?from={from}&to={to}
     * Get correctness rate within a time range
     */
    @GetMapping("/me/correctness-rate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Double>> getCorrectnessRate(
            Authentication authentication,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        Long userId = Long.parseLong(authentication.getName());
        log.debug("GET /v1/attempts/me/correctness-rate - Calculating rate for user id: {} from {} to {}",
                userId, from, to);

        double rate = exerciseAttemptService.getCorrectnessRate(userId, from, to);
        return ResponseEntity.ok(Map.of("correctnessRate", rate));
    }

    // ==================== Admin Endpoints ====================

    /**
     * GET /v1/attempts/users/{userId}
     * Get all attempts for a specific user (Admin only)
     */
    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ExerciseAttemptResponse>> getUserAttempts(@PathVariable("userId") Long userId) {
        log.debug("GET /v1/attempts/users/{} - Admin fetching attempts", userId);

        List<ExerciseAttemptResponse> attempts = exerciseAttemptService.getUserAttempts(userId);
        return ResponseEntity.ok(attempts);
    }

    /**
     * GET /v1/attempts/users/{userId}/statistics
     * Get statistics for a specific user (Admin only)
     */
    @GetMapping("/users/{userId}/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getUserStatistics(@PathVariable("userId") Long userId) {
        log.debug("GET /v1/attempts/users/{}/statistics - Admin fetching user stats", userId);

        Map<String, Object> stats = exerciseAttemptService.getUserStatistics(userId);
        return ResponseEntity.ok(stats);
    }
}
