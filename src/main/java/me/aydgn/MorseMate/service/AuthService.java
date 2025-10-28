package me.aydgn.MorseMate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.dto.request.LoginRequest;
import me.aydgn.MorseMate.dto.request.RegisterRequest;
import me.aydgn.MorseMate.dto.response.AuthResponse;
import me.aydgn.MorseMate.dto.response.UserResponse;
import me.aydgn.MorseMate.entity.User;
import me.aydgn.MorseMate.repository.UserRepository;
import me.aydgn.MorseMate.security.JwtUtil;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final me.aydgn.MorseMate.security.RateLimitService rateLimitService;

    /**
     * Register a new user with enhanced security validations
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registration attempt for username: {}", request.getUsername());

        // Normalize and validate inputs
        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase();
        String password = request.getPassword();

        // Rate limiting check (by email to prevent spam registrations)
        String rateLimitKey = "register:" + email;
        if (rateLimitService.isBlocked(rateLimitKey)) {
            throw new IllegalArgumentException("Too many registration attempts. Please try again later.");
        }

        // Validate username format (alphanumeric, underscore, hyphen only)
        if (!username.matches("^[a-zA-Z0-9_-]{3,50}$")) {
            log.warn("Invalid username format: {}", username);
            throw new IllegalArgumentException("Username must be 3-50 characters and contain only letters, numbers, underscore, or hyphen");
        }

        // Validate email format more strictly
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            log.warn("Invalid email format: {}", email);
            throw new IllegalArgumentException("Invalid email format");
        }

        // Enhanced password validation
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new IllegalArgumentException("Password must contain at least one lowercase letter");
        }
        if (!password.matches(".*[0-9].*")) {
            throw new IllegalArgumentException("Password must contain at least one number");
        }

        // Check for common weak passwords
        String[] commonPasswords = {"password", "12345678", "qwerty", "abc123", "password123", "admin123"};
        String lowerPassword = password.toLowerCase();
        for (String common : commonPasswords) {
            if (lowerPassword.contains(common)) {
                log.warn("Weak password detected for user: {}", username);
                throw new IllegalArgumentException("Password is too common or weak");
            }
        }

        // Check if username already exists (case-insensitive)
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            log.warn("Username already exists: {}", username);
            throw new IllegalArgumentException("Username already exists");
        }

        // Check if email already exists (case-insensitive)
        if (userRepository.existsByEmailIgnoreCase(email)) {
            log.warn("Email already exists: {}", email);
            throw new IllegalArgumentException("Email already exists");
        }

        // Validate full name if provided
        String fullName = request.getFullName();
        if (fullName != null && !fullName.trim().isEmpty()) {
            fullName = fullName.trim();
            if (fullName.length() > 100) {
                throw new IllegalArgumentException("Full name must not exceed 100 characters");
            }
            // Only allow letters, spaces, hyphens, and apostrophes
            if (!fullName.matches("^[a-zA-Z\\s'-]+$")) {
                throw new IllegalArgumentException("Full name contains invalid characters");
            }
        }

        // Create new user with secure defaults
        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(password)) // BCrypt with salt
                .fullName(fullName)
                .level(1)
                .totalPoints(0)
                .currentStreak(0)
                .longestStreak(0)
                .hearts(5)
                .maxHearts(5)
                .isActive(true)
                .emailVerified(false) // Email verification required
                .lastHeartRefill(LocalDateTime.now())
                .build();

        try {
            user = userRepository.save(user);
            log.info("User registered successfully: {} (ID: {})", user.getUsername(), user.getId());

            // Clear rate limit on successful registration
            rateLimitService.registerSuccess(rateLimitKey);

            // Generate JWT token with role
            String accessToken = jwtUtil.generateToken(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getRole().name()
            );

            // Build response (don't expose sensitive data)
            return AuthResponse.ofTokens(
                    accessToken,
                    null, // refresh token (implement separately for enhanced security)
                    jwtUtil.getExpirationInSeconds(),
                    UserResponse.from(user)
            );
        } catch (Exception e) {
            // Register failed attempt for rate limiting
            rateLimitService.registerAttempt(rateLimitKey);
            throw e;
        }
    }

    /**
     * Authenticate user and generate JWT token with rate limiting
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        String identifier = request.getIdentifier().trim();
        log.info("Login attempt for: {}", identifier);

        // Rate limiting check (prevent brute force attacks)
        String rateLimitKey = "login:" + identifier.toLowerCase();
        if (rateLimitService.isBlocked(rateLimitKey)) {
            int remainingMinutes = 15; // Lockout duration
            throw new BadCredentialsException("Too many failed login attempts. Account locked for " + remainingMinutes + " minutes.");
        }

        // Determine if identifier is email or username
        User user;

        if (identifier.contains("@")) {
            // Treat as email
            user = userRepository.findByEmailIgnoreCase(identifier)
                    .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        } else {
            // Treat as username
            user = userRepository.findByUsernameIgnoreCase(identifier)
                    .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        }

        // Check if user is active
        if (!user.getIsActive()) {
            throw new BadCredentialsException("Account is inactive");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            // Register failed attempt for rate limiting
            rateLimitService.registerAttempt(rateLimitKey);

            int remaining = rateLimitService.getRemainingAttempts(rateLimitKey);
            log.warn("Failed login attempt for: {}. Remaining attempts: {}", identifier, remaining);

            if (remaining > 0) {
                throw new BadCredentialsException("Invalid credentials. " + remaining + " attempts remaining.");
            } else {
                throw new BadCredentialsException("Invalid credentials. Account locked for 15 minutes.");
            }
        }

        // Clear rate limit on successful login
        rateLimitService.registerSuccess(rateLimitKey);

        // Update last login timestamp
        userRepository.touchLastLogin(user.getId(), LocalDateTime.now());
        user.setLastLogin(LocalDateTime.now());

        log.info("User logged in successfully: {}", user.getUsername());

        // Generate JWT token with role
        String accessToken = jwtUtil.generateToken(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name()
        );

        // Build response
        return AuthResponse.ofTokens(
                accessToken,
                null, // refresh token (optional)
                jwtUtil.getExpirationInSeconds(),
                UserResponse.from(user)
        );
    }

    /**
     * Get current authenticated user
     */
    @Transactional(readOnly = true)
    public User getCurrentUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
