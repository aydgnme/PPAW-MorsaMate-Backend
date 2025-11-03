package me.aydgn.MorseMate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.dto.response.ApiMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for health check and status endpoints
 * Provides basic API health monitoring capabilities
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class HealthController {

    /**
     * GET /api/health
     * Basic health check endpoint
     * Public endpoint - no authentication required
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        log.debug("GET /api/health - Health check requested");

        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "MorseMate API");

        return ResponseEntity.ok(health);
    }

    /**
     * GET /api/ping
     * Simple ping endpoint
     * Public endpoint - no authentication required
     */
    @GetMapping("/ping")
    public ResponseEntity<ApiMessage> ping() {
        log.debug("GET /api/ping - Ping requested");
        return ResponseEntity.ok(new ApiMessage("pong"));
    }
}
