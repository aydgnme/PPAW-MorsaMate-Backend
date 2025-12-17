package me.aydgn.MorseMate.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(
            @Value("${app.cache.ttl-minutes:60}") long ttlMinutes,
            @Value("${app.cache.max-size:1000}") long maximumSize
    ) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "categories",
                "lessons",
                "subscription-plans",
                "user-subscriptions"
        );

        cacheManager.setCaffeine(
                Caffeine.newBuilder()
                        .expireAfterWrite(Duration.ofMinutes(ttlMinutes))
                        .maximumSize(maximumSize)
        );
        cacheManager.setAllowNullValues(false);
        return cacheManager;
    }
}
