package me.aydgn.MorseMate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.dto.response.LeaderboardEntryResponse;
import me.aydgn.MorseMate.service.LeaderboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Leaderboard management
 * Provides endpoints for viewing leaderboards and user rankings
 */
@RestController
@RequestMapping("/v1/leaderboard")
@RequiredArgsConstructor
@Slf4j
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    /**
     * GET /v1/leaderboard/points
     * Get top users by total points
     */
    @GetMapping("/points")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LeaderboardEntryResponse>> getTopUsersByPoints(
            @RequestParam(defaultValue = "10") int limit) {
        log.debug("GET /v1/leaderboard/points - Fetching top {} users", limit);

        if (limit < 1) limit = 10;
        if (limit > 100) limit = 100;

        List<LeaderboardEntryResponse> leaderboard = leaderboardService.getTopUsersByPoints(limit);
        return ResponseEntity.ok(leaderboard);
    }

    /**
     * GET /v1/leaderboard/level
     * Get top users by level
     */
    @GetMapping("/level")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LeaderboardEntryResponse>> getTopUsersByLevel(
            @RequestParam(defaultValue = "10") int limit) {
        log.debug("GET /v1/leaderboard/level - Fetching top {} users", limit);

        if (limit < 1) limit = 10;
        if (limit > 100) limit = 100;

        List<LeaderboardEntryResponse> leaderboard = leaderboardService.getTopUsersByLevel(limit);
        return ResponseEntity.ok(leaderboard);
    }

    /**
     * GET /v1/leaderboard/streak
     * Get top users by current streak
     */
    @GetMapping("/streak")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LeaderboardEntryResponse>> getTopUsersByStreak(
            @RequestParam(defaultValue = "10") int limit) {
        log.debug("GET /v1/leaderboard/streak - Fetching top {} users", limit);

        if (limit < 1) limit = 10;
        if (limit > 100) limit = 100;

        List<LeaderboardEntryResponse> leaderboard = leaderboardService.getTopUsersByStreak(limit);
        return ResponseEntity.ok(leaderboard);
    }

    /**
     * GET /v1/leaderboard/longest-streak
     * Get top users by longest streak
     */
    @GetMapping("/longest-streak")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LeaderboardEntryResponse>> getTopUsersByLongestStreak(
            @RequestParam(defaultValue = "10") int limit) {
        log.debug("GET /v1/leaderboard/longest-streak - Fetching top {} users", limit);

        if (limit < 1) limit = 10;
        if (limit > 100) limit = 100;

        List<LeaderboardEntryResponse> leaderboard = leaderboardService.getTopUsersByLongestStreak(limit);
        return ResponseEntity.ok(leaderboard);
    }

    /**
     * GET /v1/leaderboard/achievements
     * Get top users by achievements count
     */
    @GetMapping("/achievements")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LeaderboardEntryResponse>> getTopUsersByAchievements(
            @RequestParam(defaultValue = "10") int limit) {
        log.debug("GET /v1/leaderboard/achievements - Fetching top {} users", limit);

        if (limit < 1) limit = 10;
        if (limit > 100) limit = 100;

        List<LeaderboardEntryResponse> leaderboard = leaderboardService.getTopUsersByAchievements(limit);
        return ResponseEntity.ok(leaderboard);
    }

    /**
     * GET /v1/leaderboard/me
     * Get authenticated user's leaderboard position
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LeaderboardEntryResponse> getMyLeaderboardPosition(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        log.debug("GET /v1/leaderboard/me - Fetching position for user id: {}", userId);

        LeaderboardEntryResponse position = leaderboardService.getUserLeaderboardPosition(userId);
        return ResponseEntity.ok(position);
    }

    /**
     * GET /v1/leaderboard/me/rank/points
     * Get authenticated user's rank by points
     */
    @GetMapping("/me/rank/points")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Long>> getMyRankByPoints(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        log.debug("GET /v1/leaderboard/me/rank/points - Fetching for user id: {}", userId);

        Long rank = leaderboardService.getUserRankByPoints(userId);
        return ResponseEntity.ok(Map.of("rank", rank != null ? rank : 0L));
    }

    /**
     * GET /v1/leaderboard/me/rank/level
     * Get authenticated user's rank by level
     */
    @GetMapping("/me/rank/level")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Long>> getMyRankByLevel(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        log.debug("GET /v1/leaderboard/me/rank/level - Fetching for user id: {}", userId);

        Long rank = leaderboardService.getUserRankByLevel(userId);
        return ResponseEntity.ok(Map.of("rank", rank != null ? rank : 0L));
    }

    /**
     * GET /v1/leaderboard/me/rank/streak
     * Get authenticated user's rank by current streak
     */
    @GetMapping("/me/rank/streak")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Long>> getMyRankByStreak(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        log.debug("GET /v1/leaderboard/me/rank/streak - Fetching for user id: {}", userId);

        Long rank = leaderboardService.getUserRankByStreak(userId);
        return ResponseEntity.ok(Map.of("rank", rank != null ? rank : 0L));
    }

    /**
     * GET /v1/leaderboard/me/rank/achievements
     * Get authenticated user's rank by achievements
     */
    @GetMapping("/me/rank/achievements")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Long>> getMyRankByAchievements(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        log.debug("GET /v1/leaderboard/me/rank/achievements - Fetching for user id: {}", userId);

        Long rank = leaderboardService.getUserRankByAchievements(userId);
        return ResponseEntity.ok(Map.of("rank", rank != null ? rank : 0L));
    }
}
