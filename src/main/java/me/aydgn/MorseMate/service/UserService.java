package me.aydgn.MorseMate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.dto.request.UpdateProfileRequest;
import me.aydgn.MorseMate.dto.response.UserResponse;
import me.aydgn.MorseMate.entity.User;
import me.aydgn.MorseMate.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    /**
     * Get the currently authenticated user from the security context.
     * 
     * @return The authenticated User entity.
     * @throws RuntimeException if no user is authenticated.
     */
    @Transactional(readOnly = true)
    public User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            throw new RuntimeException("No authenticated user found.");
        }
        String principal = authentication.getName();
        try {
            Long userId = Long.parseLong(principal);
            return userRepository.findById(userId)
                    .orElseThrow(
                            () -> new RuntimeException("Authenticated user not found in database (ID): " + userId));
        } catch (NumberFormatException e) {
            return userRepository.findByUsernameIgnoreCase(principal)
                    .orElseThrow(() -> new RuntimeException(
                            "Authenticated user not found in database (Username): " + principal));
        }
    }

    /**
     * Get the currently authenticated user if present.
     * 
     * @return Optional of the authenticated User entity.
     */
    @Transactional(readOnly = true)
    public Optional<User> getAuthenticatedUser() {
        try {
            return Optional.of(getCurrentAuthenticatedUser());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
    }

    /**
     * Get user response by ID
     */
    @Transactional(readOnly = true)
    public UserResponse getUserResponseById(Long userId) {
        User user = getUserById(userId);
        return UserResponse.from(user);
    }

    /**
     * Get user by username
     */
    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));
    }

    /**
     * Get user by email
     */
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
    }

    /**
     * Update user profile
     */
    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = getUserById(userId);

        // Update full name if provided
        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            String fullName = request.getFullName().trim();
            if (fullName.length() > 100) {
                throw new IllegalArgumentException("Full name must not exceed 100 characters");
            }
            if (!fullName.matches("^[a-zA-Z\\s'-]+$")) {
                throw new IllegalArgumentException("Full name contains invalid characters");
            }
            user.setFullName(fullName);
        }

        // Update profile picture URL if provided
        if (request.getProfilePictureUrl() != null && !request.getProfilePictureUrl().trim().isEmpty()) {
            String url = request.getProfilePictureUrl().trim();
            if (url.length() > 255) {
                throw new IllegalArgumentException("Profile picture URL must not exceed 255 characters");
            }
            user.setProfilePictureUrl(url);
        }

        user = userRepository.save(user);
        log.info("Profile updated for user: {} (ID: {})", user.getUsername(), user.getId());

        return UserResponse.from(user);
    }

    /**
     * Add points to user
     */
    @Transactional
    public void addPoints(Long userId, int points) {
        if (points <= 0) {
            throw new IllegalArgumentException("Points must be positive");
        }

        int updated = userRepository.addPointsAndRelevel(userId, points);
        if (updated == 0) {
            throw new IllegalArgumentException("User not found with id: " + userId);
        }

        log.info("Added {} points to user ID: {}", points, userId);
    }

    /**
     * Use one heart
     */
    @Transactional
    public boolean useHeart(Long userId) {
        int updated = userRepository.useOneHeartIfAvailable(userId);
        boolean success = updated > 0;

        if (success) {
            log.info("Heart used by user ID: {}", userId);
        } else {
            log.warn("No hearts available for user ID: {}", userId);
        }

        return success;
    }

    /**
     * Refill hearts
     */
    @Transactional
    public void refillHearts(Long userId) {
        int updated = userRepository.refillHeartsIfNotFull(userId, LocalDateTime.now());

        if (updated > 0) {
            log.info("Hearts refilled for user ID: {}", userId);
        } else {
            log.info("Hearts already full for user ID: {}", userId);
        }
    }

    /**
     * Get all users (paginated)
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(UserResponse::from);
    }

    /**
     * Deactivate user account
     */
    @Transactional
    public void deactivateAccount(Long userId) {
        User user = getUserById(userId);
        user.setIsActive(false);
        userRepository.save(user);

        log.warn("User account deactivated: {} (ID: {})", user.getUsername(), user.getId());
    }

    /**
     * Activate user account
     */
    @Transactional
    public void activateAccount(Long userId) {
        User user = getUserById(userId);
        user.setIsActive(true);
        userRepository.save(user);

        log.info("User account activated: {} (ID: {})", user.getUsername(), user.getId());
    }

    /**
     * Verify user email
     */
    @Transactional
    public void verifyEmail(Long userId) {
        User user = getUserById(userId);
        user.setEmailVerified(true);
        userRepository.save(user);

        log.info("Email verified for user: {} (ID: {})", user.getUsername(), user.getId());
    }

    /**
     * Get user statistics
     */
    @Transactional(readOnly = true)
    public UserStatistics getUserStatistics(Long userId) {
        User user = getUserById(userId);

        return UserStatistics.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .level(user.getLevel())
                .totalPoints(user.getTotalPoints())
                .currentStreak(user.getCurrentStreak())
                .longestStreak(user.getLongestStreak())
                .hearts(user.getHearts())
                .maxHearts(user.getMaxHearts())
                .accountAge(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .build();
    }

    // DTO for user statistics
    @lombok.Data
    @lombok.Builder
    public static class UserStatistics {
        private Long userId;
        private String username;
        private Integer level;
        private Integer totalPoints;
        private Integer currentStreak;
        private Integer longestStreak;
        private Integer hearts;
        private Integer maxHearts;
        private LocalDateTime accountAge;
        private LocalDateTime lastLogin;
    }
}
