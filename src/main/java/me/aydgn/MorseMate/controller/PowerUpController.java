package me.aydgn.MorseMate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.dto.request.CreatePowerUpRequest;
import me.aydgn.MorseMate.dto.request.UpdatePowerUpRequest;
import me.aydgn.MorseMate.dto.response.ApiMessage;
import me.aydgn.MorseMate.dto.response.PowerUpResponse;
import me.aydgn.MorseMate.dto.response.UserPowerUpResponse;
import me.aydgn.MorseMate.entity.PowerUp;
import me.aydgn.MorseMate.service.PowerUpService;
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
 * REST Controller for PowerUp management
 * Provides endpoints for managing power-ups and user purchases
 */
@RestController
@RequestMapping("/v1/powerups")
@RequiredArgsConstructor
@Slf4j
public class PowerUpController {

    private final PowerUpService powerUpService;

    /**
     * GET /v1/powerups/me
     * Get authenticated user's power-ups
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserPowerUpResponse>> getMyPowerUps(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        log.debug("GET /v1/powerups/me - Fetching power-ups for user id: {}", userId);
        List<UserPowerUpResponse> powerUps = powerUpService.getUserPowerUps(userId);
        return ResponseEntity.ok(powerUps);
    }

    /**
     * GET /v1/powerups/me/paged
     * Get authenticated user's power-ups with pagination
     */
    @GetMapping("/me/paged")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<UserPowerUpResponse>> getMyPowerUpsPaged(
            Authentication authentication,
            @PageableDefault(size = 20, sort = "purchasedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Long userId = Long.parseLong(authentication.getName());
        log.debug("GET /v1/powerups/me/paged - Fetching paged power-ups for user id: {}", userId);
        Page<UserPowerUpResponse> powerUps = powerUpService.getUserPowerUpsPaged(userId, pageable);
        return ResponseEntity.ok(powerUps);
    }

    /**
     * GET /v1/powerups/me/active
     * Get authenticated user's active power-ups
     */
    @GetMapping("/me/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserPowerUpResponse>> getMyActivePowerUps(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        log.debug("GET /v1/powerups/me/active - Fetching active power-ups for user id: {}", userId);
        List<UserPowerUpResponse> activePowerUps = powerUpService.getUserActivePowerUps(userId);
        return ResponseEntity.ok(activePowerUps);
    }

    /**
     * POST /v1/powerups/me/purchase/{powerUpId}
     * Purchase power-up for authenticated user
     */
    @PostMapping("/me/purchase/{powerUpId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserPowerUpResponse> purchasePowerUp(
            Authentication authentication,
            @PathVariable Long powerUpId) {
        Long userId = Long.parseLong(authentication.getName());
        log.info("POST /v1/powerups/me/purchase/{} - User {} purchasing", powerUpId, userId);
        UserPowerUpResponse userPowerUp = powerUpService.purchasePowerUp(userId, powerUpId);
        return ResponseEntity.ok(userPowerUp);
    }

    /**
     * POST /v1/powerups/me/activate/{userPowerUpId}
     * Activate power-up for authenticated user
     */
    @PostMapping("/me/activate/{userPowerUpId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserPowerUpResponse> activatePowerUp(
            Authentication authentication,
            @PathVariable Long userPowerUpId) {
        Long userId = Long.parseLong(authentication.getName());
        log.info("POST /v1/powerups/me/activate/{} - User {} activating", userPowerUpId, userId);
        UserPowerUpResponse userPowerUp = powerUpService.activatePowerUp(userId, userPowerUpId);
        return ResponseEntity.ok(userPowerUp);
    }

    /**
     * GET /v1/powerups/me/check/{type}
     * Check if authenticated user has active power-up of specific type
     */
    @GetMapping("/me/check/{type}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Boolean>> checkActivePowerUp(
            Authentication authentication,
            @PathVariable String type) {
        Long userId = Long.parseLong(authentication.getName());
        log.debug("GET /v1/powerups/me/check/{} - Checking for user id: {}", type, userId);

        PowerUp.Type powerUpType = PowerUp.Type.valueOf(type.toUpperCase());
        boolean hasActive = powerUpService.hasActivePowerUp(userId, powerUpType);
        return ResponseEntity.ok(Map.of("hasActive", hasActive));
    }

    // ==================== Admin Endpoints ====================

    /**
     * GET /v1/powerups
     * Get all power-ups (Admin only)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PowerUpResponse>> getAllPowerUps() {
        log.debug("GET /v1/powerups - Admin fetching all power-ups");
        List<PowerUpResponse> powerUps = powerUpService.getAllPowerUps();
        return ResponseEntity.ok(powerUps);
    }

    /**
     * GET /v1/powerups/active
     * Get all active power-ups (Admin only)
     */
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PowerUpResponse>> getAllActivePowerUps() {
        log.debug("GET /v1/powerups/active - Admin fetching active power-ups");
        List<PowerUpResponse> powerUps = powerUpService.getAllActivePowerUps();
        return ResponseEntity.ok(powerUps);
    }

    /**
     * GET /v1/powerups/paged
     * Get all power-ups with pagination (Admin only)
     */
    @GetMapping("/paged")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<PowerUpResponse>> getAllPowerUpsPaged(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.debug("GET /v1/powerups/paged - Admin fetching paged power-ups");
        Page<PowerUpResponse> powerUps = powerUpService.getAllPowerUpsPaged(pageable);
        return ResponseEntity.ok(powerUps);
    }

    /**
     * GET /v1/powerups/{id}
     * Get power-up by ID (Admin only)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PowerUpResponse> getPowerUpById(@PathVariable Long id) {
        log.debug("GET /v1/powerups/{} - Admin fetching power-up", id);
        PowerUpResponse powerUp = powerUpService.getPowerUpById(id);
        return ResponseEntity.ok(powerUp);
    }

    /**
     * GET /v1/powerups/type/{type}
     * Get power-ups by type (Admin only)
     */
    @GetMapping("/type/{type}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PowerUpResponse>> getPowerUpsByType(@PathVariable String type) {
        log.debug("GET /v1/powerups/type/{} - Admin fetching", type);
        PowerUp.Type powerUpType = PowerUp.Type.valueOf(type.toUpperCase());
        List<PowerUpResponse> powerUps = powerUpService.getPowerUpsByType(powerUpType);
        return ResponseEntity.ok(powerUps);
    }

    /**
     * POST /v1/powerups
     * Create a new power-up (Admin only)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PowerUpResponse> createPowerUp(@Valid @RequestBody CreatePowerUpRequest request) {
        log.info("POST /v1/powerups - Admin creating power-up: {}", request.getName());
        PowerUpResponse powerUp = powerUpService.createPowerUp(request);
        return ResponseEntity.ok(powerUp);
    }

    /**
     * PUT /v1/powerups/{id}
     * Update an existing power-up (Admin only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PowerUpResponse> updatePowerUp(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePowerUpRequest request) {
        log.info("PUT /v1/powerups/{} - Admin updating power-up", id);
        PowerUpResponse powerUp = powerUpService.updatePowerUp(id, request);
        return ResponseEntity.ok(powerUp);
    }

    /**
     * DELETE /v1/powerups/{id}
     * Delete a power-up (Admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiMessage> deletePowerUp(@PathVariable Long id) {
        log.info("DELETE /v1/powerups/{} - Admin deleting power-up", id);
        powerUpService.deletePowerUp(id);
        return ResponseEntity.ok(new ApiMessage("PowerUp deleted successfully"));
    }

    /**
     * GET /v1/powerups/count
     * Get total power-up count (Admin only)
     */
    @GetMapping("/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> getTotalPowerUpCount() {
        log.debug("GET /v1/powerups/count - Admin fetching count");
        long count = powerUpService.getTotalPowerUpCount();
        return ResponseEntity.ok(Map.of("totalCount", count));
    }

    /**
     * GET /v1/powerups/count/active
     * Get active power-up count (Admin only)
     */
    @GetMapping("/count/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> getActivePowerUpCount() {
        log.debug("GET /v1/powerups/count/active - Admin fetching active count");
        long count = powerUpService.getActivePowerUpCount();
        return ResponseEntity.ok(Map.of("activeCount", count));
    }

    /**
     * POST /v1/powerups/cleanup
     * Cleanup expired power-ups (Admin only)
     */
    @PostMapping("/cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiMessage> cleanupExpiredPowerUps() {
        log.info("POST /v1/powerups/cleanup - Admin triggering cleanup");
        powerUpService.cleanupExpiredPowerUps();
        return ResponseEntity.ok(new ApiMessage("Expired power-ups cleaned up successfully"));
    }
}
