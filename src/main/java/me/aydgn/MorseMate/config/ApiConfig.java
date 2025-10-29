package me.aydgn.MorseMate.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * API Configuration
 * Manages API version and base paths dynamically from application.properties
 */
@Configuration
@Getter
public class ApiConfig {

    @Value("${api.version:v1}")
    private String version;

    /**
     * Get the base API path with version
     * Example: /v1
     */
    public String getBasePath() {
        return "/" + version;
    }

    /**
     * Get full path with endpoint
     * Example: /v1/users
     */
    public String getPath(String endpoint) {
        return getBasePath() + "/" + endpoint;
    }
}
