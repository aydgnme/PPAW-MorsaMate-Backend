package me.aydgn.MorseMate.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}") // default 24h in ms
    private Long jwtExpiration;

    private static final int MIN_KEY_LENGTH = 64; // 512 bits minimum for HS512

    private SecretKey signingKey;

    /**
     * Validate JWT secret key on application startup
     */
    @PostConstruct
    public void validateSecretKey() {
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            throw new IllegalStateException("JWT secret key is not configured. Please set JWT_SECRET environment variable.");
        }

        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < MIN_KEY_LENGTH) {
            // Derive a deterministic 512-bit key from the provided secret using SHA-512
            try {
                MessageDigest sha512 = MessageDigest.getInstance("SHA-512");
                keyBytes = sha512.digest(keyBytes); // 64 bytes
                log.warn("JWT secret was {} bytes; derived a 512-bit key via SHA-512 for HS512.", jwtSecret.length());
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException("SHA-512 not available to derive HS512 key", e);
            }
        }

        signingKey = Keys.hmacShaKeyFor(keyBytes);
        log.info("JWT secret key ready ({} bytes used for HS512)", keyBytes.length);
    }

    private SecretKey getSigningKey() {
        if (signingKey == null) {
            throw new IllegalStateException("Signing key not initialized. validateSecretKey() should have run at startup.");
        }
        return signingKey;
    }

    /**
     * Generate JWT token for a user
     */
    public String generateToken(Long userId, String username, String email, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
            .subject(userId.toString())
            .claim("username", username)
            .claim("email", email)
            .claim("role", role)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(getSigningKey(), Jwts.SIG.HS512)
            .compact();
    }

    /**
     * Extract user ID from JWT token
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * Extract username from JWT token
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }

    /**
     * Extract email from JWT token
     */
    public String getEmailFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("email", String.class);
    }

    /**
     * Extract role from JWT token
     */
    public String getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("role", String.class);
    }

    /**
     * Validate JWT token
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (MalformedJwtException ex) {
            // Invalid JWT token
        } catch (ExpiredJwtException ex) {
            // Expired JWT token
        } catch (UnsupportedJwtException ex) {
            // Unsupported JWT token
        } catch (IllegalArgumentException ex) {
            // JWT claims string is empty
        }
        return false;
    }

    /**
     * Parse JWT token and extract claims
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Get token expiration time in seconds
     */
    public Long getExpirationInSeconds() {
        return jwtExpiration / 1000;
    }
}
