package me.aydgn.MorseMate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class PingController {

    private final DataSource dataSource;

    /**
     * Simple ping endpoint
     * GET /ping
     */
    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

    /**
     * Health check endpoint with database connection check
     * GET /health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        health.put("service", "MorseMate Backend");
        health.put("version", "1.0.0");

        // Check database connection
        Map<String, Object> database = new HashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            boolean isValid = connection.isValid(2); // 2 second timeout
            database.put("status", isValid ? "UP" : "DOWN");
            database.put("database", connection.getMetaData().getDatabaseProductName());
            database.put("version", connection.getMetaData().getDatabaseProductVersion());
            database.put("url", connection.getMetaData().getURL());
        } catch (Exception e) {
            database.put("status", "DOWN");
            database.put("error", e.getMessage());
            health.put("status", "DOWN");
        }
        health.put("database", database);

        // Check memory
        Map<String, Object> memory = new HashMap<>();
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        memory.put("max", formatBytes(maxMemory));
        memory.put("total", formatBytes(totalMemory));
        memory.put("used", formatBytes(usedMemory));
        memory.put("free", formatBytes(freeMemory));
        memory.put("usagePercent", String.format("%.2f%%", (usedMemory * 100.0) / totalMemory));
        health.put("memory", memory);

        // Return appropriate status code
        HttpStatus status = "UP".equals(health.get("status")) ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(status).body(health);
    }

    /**
     * Detailed system check endpoint
     * GET /system-check
     */
    @GetMapping("/system-check")
    public ResponseEntity<Map<String, Object>> systemCheck() {
        Map<String, Object> systemInfo = new HashMap<>();
        systemInfo.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        // Backend service info
        Map<String, Object> backend = new HashMap<>();
        backend.put("status", "RUNNING");
        backend.put("service", "MorseMate Backend");
        backend.put("version", "1.0.0");
        backend.put("environment", System.getProperty("spring.profiles.active", "default"));
        systemInfo.put("backend", backend);

        // Database connection check
        Map<String, Object> database = new HashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            boolean isValid = connection.isValid(2);
            database.put("status", isValid ? "CONNECTED" : "DISCONNECTED");
            database.put("database", connection.getMetaData().getDatabaseProductName());
            database.put("version", connection.getMetaData().getDatabaseProductVersion());
            database.put("driver", connection.getMetaData().getDriverName());
            database.put("driverVersion", connection.getMetaData().getDriverVersion());
            database.put("url", maskPassword(connection.getMetaData().getURL()));
            database.put("catalog", connection.getCatalog());
            database.put("autoCommit", connection.getAutoCommit());
            database.put("readOnly", connection.isReadOnly());
        } catch (Exception e) {
            database.put("status", "ERROR");
            database.put("error", e.getMessage());
        }
        systemInfo.put("database", database);

        // JVM information
        Map<String, Object> jvm = new HashMap<>();
        Runtime runtime = Runtime.getRuntime();
        jvm.put("javaVersion", System.getProperty("java.version"));
        jvm.put("javaVendor", System.getProperty("java.vendor"));
        jvm.put("jvmName", System.getProperty("java.vm.name"));
        jvm.put("jvmVersion", System.getProperty("java.vm.version"));
        jvm.put("osName", System.getProperty("os.name"));
        jvm.put("osVersion", System.getProperty("os.version"));
        jvm.put("osArch", System.getProperty("os.arch"));
        jvm.put("availableProcessors", runtime.availableProcessors());
        systemInfo.put("jvm", jvm);

        // Memory information
        Map<String, Object> memory = new HashMap<>();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        memory.put("maxMemory", formatBytes(maxMemory));
        memory.put("totalMemory", formatBytes(totalMemory));
        memory.put("usedMemory", formatBytes(usedMemory));
        memory.put("freeMemory", formatBytes(freeMemory));
        memory.put("usagePercent", String.format("%.2f%%", (usedMemory * 100.0) / totalMemory));
        systemInfo.put("memory", memory);

        // Thread information
        Map<String, Object> threads = new HashMap<>();
        ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
        while (rootGroup.getParent() != null) {
            rootGroup = rootGroup.getParent();
        }
        threads.put("activeThreads", rootGroup.activeCount());
        threads.put("activeThreadGroups", rootGroup.activeGroupCount());
        systemInfo.put("threads", threads);

        // Overall status
        boolean allHealthy = "CONNECTED".equals(database.get("status"));
        systemInfo.put("overallStatus", allHealthy ? "HEALTHY" : "DEGRADED");

        return ResponseEntity.ok(systemInfo);
    }

    /**
     * Database connection test endpoint
     * GET /db-check
     */
    @GetMapping("/db-check")
    public ResponseEntity<Map<String, Object>> databaseCheck() {
        Map<String, Object> dbInfo = new HashMap<>();
        dbInfo.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        try (Connection connection = dataSource.getConnection()) {
            boolean isValid = connection.isValid(2);

            dbInfo.put("connected", isValid);
            dbInfo.put("status", isValid ? "SUCCESS" : "FAILED");
            dbInfo.put("productName", connection.getMetaData().getDatabaseProductName());
            dbInfo.put("productVersion", connection.getMetaData().getDatabaseProductVersion());
            dbInfo.put("driverName", connection.getMetaData().getDriverName());
            dbInfo.put("driverVersion", connection.getMetaData().getDriverVersion());
            dbInfo.put("url", maskPassword(connection.getMetaData().getURL()));
            dbInfo.put("catalog", connection.getCatalog());
            dbInfo.put("schema", connection.getSchema());
            dbInfo.put("transactionIsolation", connection.getTransactionIsolation());
            dbInfo.put("autoCommit", connection.getAutoCommit());

            return ResponseEntity.ok(dbInfo);
        } catch (Exception e) {
            dbInfo.put("connected", false);
            dbInfo.put("status", "ERROR");
            dbInfo.put("error", e.getMessage());
            dbInfo.put("errorType", e.getClass().getSimpleName());

            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(dbInfo);
        }
    }

    /**
     * Format bytes to human-readable format
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.2f %sB", bytes / Math.pow(1024, exp), pre);
    }

    /**
     * Mask password in JDBC URL
     */
    private String maskPassword(String url) {
        if (url == null) return null;
        return url.replaceAll("password=[^&;]*", "password=****");
    }
}

