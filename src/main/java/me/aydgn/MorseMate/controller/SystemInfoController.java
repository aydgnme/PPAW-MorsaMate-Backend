package me.aydgn.MorseMate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.dto.response.SystemInfoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootVersion;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * REST Controller for system information endpoints
 * Provides application metadata and version information
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class SystemInfoController {

    @Value("${spring.application.name:MorseMate}")
    private String applicationName;

    @Value("${app.version:1.0.0}")
    private String version;

    @Value("${spring.profiles.active:default}")
    private String environment;

    /**
     * GET /api/info
     * Get system information
     * Public endpoint - no authentication required
     */
    @GetMapping("/info")
    public ResponseEntity<SystemInfoResponse> getSystemInfo() {
        log.debug("GET /api/info - System info requested");

        SystemInfoResponse info = SystemInfoResponse.builder()
                .applicationName(applicationName)
                .version(version)
                .environment(environment)
                .serverTime(LocalDateTime.now())
                .javaVersion(System.getProperty("java.version"))
                .springBootVersion(SpringBootVersion.getVersion())
                .build();

        return ResponseEntity.ok(info);
    }
}
