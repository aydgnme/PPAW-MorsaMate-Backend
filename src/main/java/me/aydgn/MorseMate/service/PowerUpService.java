package me.aydgn.MorseMate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.dto.request.CreatePowerUpRequest;
import me.aydgn.MorseMate.dto.request.UpdatePowerUpRequest;
import me.aydgn.MorseMate.dto.response.PowerUpResponse;
import me.aydgn.MorseMate.dto.response.UserPowerUpResponse;
import me.aydgn.MorseMate.entity.PowerUp;
import me.aydgn.MorseMate.entity.User;
import me.aydgn.MorseMate.entity.UserPowerUp;
import me.aydgn.MorseMate.exception.InvalidOperationException;
import me.aydgn.MorseMate.exception.ResourceNotFoundException;
import me.aydgn.MorseMate.repository.PowerUpRepository;
import me.aydgn.MorseMate.repository.UserPowerUpRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PowerUpService {

    private final PowerUpRepository powerUpRepository;
    private final UserPowerUpRepository userPowerUpRepository;
    private final UserService userService;
    private final GemService gemService;

    /**
     * Get all power-ups
     */
    @Transactional(readOnly = true)
    public List<PowerUpResponse> getAllPowerUps() {
        log.debug("Fetching all power-ups");
        List<PowerUp> powerUps = powerUpRepository.findAll();
        return powerUps.stream()
                .map(PowerUpResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Get all active power-ups
     */
    @Transactional(readOnly = true)
    public List<PowerUpResponse> getAllActivePowerUps() {
        log.debug("Fetching all active power-ups");
        List<PowerUp> powerUps = powerUpRepository.findAllByIsActiveTrue();
        return powerUps.stream()
                .map(PowerUpResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Get all power-ups with pagination
     */
    @Transactional(readOnly = true)
    public Page<PowerUpResponse> getAllPowerUpsPaged(Pageable pageable) {
        log.debug("Fetching paged power-ups");
        Page<PowerUp> powerUps = powerUpRepository.findAll(pageable);
        return powerUps.map(PowerUpResponse::from);
    }

    /**
     * Get power-up by ID
     */
    @Transactional(readOnly = true)
    public PowerUpResponse getPowerUpById(Long id) {
        log.debug("Fetching power-up with id: {}", id);
        PowerUp powerUp = findPowerUpById(id);
        return PowerUpResponse.from(powerUp);
    }

    /**
     * Get power-up entity by ID (internal use)
     */
    @Transactional(readOnly = true)
    public PowerUp findPowerUpById(Long id) {
        return powerUpRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("PowerUp not found with id: {}", id);
                    return new ResourceNotFoundException("PowerUp", "id", id);
                });
    }

    /**
     * Get power-ups by type
     */
    @Transactional(readOnly = true)
    public List<PowerUpResponse> getPowerUpsByType(PowerUp.Type type) {
        log.debug("Fetching power-ups with type: {}", type);
        List<PowerUp> powerUps = powerUpRepository.findAllByType(type);
        return powerUps.stream()
                .map(PowerUpResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Create a new power-up
     */
    @Transactional
    public PowerUpResponse createPowerUp(CreatePowerUpRequest request) {
        log.debug("Creating new power-up: {}", request.getName());

        // Check if power-up with same name already exists
        if (powerUpRepository.existsByNameIgnoreCase(request.getName())) {
            log.error("PowerUp already exists with name: {}", request.getName());
            throw new InvalidOperationException(
                    String.format("PowerUp already exists with name: %s", request.getName())
            );
        }

        PowerUp.Type type;
        try {
            type = PowerUp.Type.valueOf(request.getType().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("Invalid power-up type: {}", request.getType());
            throw new InvalidOperationException(
                    String.format("Invalid power-up type: %s. Valid values are: XP_BOOST, HEART_REFILL, STREAK_FREEZE, UNLIMITED_HEARTS",
                            request.getType())
            );
        }

        PowerUp powerUp = PowerUp.builder()
                .name(request.getName())
                .description(request.getDescription())
                .type(type)
                .costGems(request.getCostGems())
                .durationHours(request.getDurationHours())
                .icon(request.getIcon())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        powerUp = powerUpRepository.save(powerUp);
        log.info("PowerUp created successfully with id: {}", powerUp.getId());

        return PowerUpResponse.from(powerUp);
    }

    /**
     * Update an existing power-up
     */
    @Transactional
    public PowerUpResponse updatePowerUp(Long id, UpdatePowerUpRequest request) {
        log.debug("Updating power-up with id: {}", id);

        PowerUp powerUp = findPowerUpById(id);

        // Update name if provided and check uniqueness
        if (request.getName() != null && !request.getName().isEmpty()) {
            if (!powerUp.getName().equalsIgnoreCase(request.getName()) &&
                    powerUpRepository.existsByNameIgnoreCase(request.getName())) {
                log.error("PowerUp already exists with name: {}", request.getName());
                throw new InvalidOperationException(
                        String.format("PowerUp already exists with name: %s", request.getName())
                );
            }
            powerUp.setName(request.getName());
        }

        if (request.getDescription() != null) {
            powerUp.setDescription(request.getDescription());
        }

        if (request.getType() != null && !request.getType().isEmpty()) {
            try {
                PowerUp.Type type = PowerUp.Type.valueOf(request.getType().toUpperCase());
                powerUp.setType(type);
            } catch (IllegalArgumentException e) {
                log.error("Invalid power-up type: {}", request.getType());
                throw new InvalidOperationException(
                        String.format("Invalid power-up type: %s", request.getType())
                );
            }
        }

        if (request.getCostGems() != null) {
            powerUp.setCostGems(request.getCostGems());
        }

        if (request.getDurationHours() != null) {
            powerUp.setDurationHours(request.getDurationHours());
        }

        if (request.getIcon() != null) {
            powerUp.setIcon(request.getIcon());
        }

        if (request.getIsActive() != null) {
            powerUp.setIsActive(request.getIsActive());
        }

        powerUp = powerUpRepository.save(powerUp);
        log.info("PowerUp updated successfully with id: {}", id);

        return PowerUpResponse.from(powerUp);
    }

    /**
     * Delete a power-up
     */
    @Transactional
    public void deletePowerUp(Long id) {
        log.debug("Deleting power-up with id: {}", id);

        PowerUp powerUp = findPowerUpById(id);
        powerUpRepository.delete(powerUp);
        log.info("PowerUp deleted successfully with id: {}", id);
    }

    /**
     * Purchase power-up for user
     */
    @Transactional
    public UserPowerUpResponse purchasePowerUp(Long userId, Long powerUpId) {
        log.debug("User {} purchasing power-up {}", userId, powerUpId);

        User user = userService.getUserById(userId);
        PowerUp powerUp = findPowerUpById(powerUpId);

        if (!powerUp.getIsActive()) {
            throw new InvalidOperationException("This power-up is not available for purchase");
        }

        // Check and spend gems
        if (!gemService.hasEnoughGems(userId, powerUp.getCostGems())) {
            throw new InvalidOperationException(
                    String.format("Insufficient gems. Required: %d", powerUp.getCostGems())
            );
        }

        gemService.spendGems(userId, powerUp.getCostGems(),
                "Power-up: " + powerUp.getName(),
                "Purchased power-up: " + powerUp.getName());

        // Create user power-up record
        UserPowerUp userPowerUp = UserPowerUp.builder()
                .user(user)
                .powerUp(powerUp)
                .purchasedAt(LocalDateTime.now())
                .isActive(false)
                .isUsed(false)
                .build();

        userPowerUp = userPowerUpRepository.save(userPowerUp);

        log.info("User {} purchased power-up {} successfully", userId, powerUpId);

        return UserPowerUpResponse.from(userPowerUp);
    }

    /**
     * Activate user power-up
     */
    @Transactional
    public UserPowerUpResponse activatePowerUp(Long userId, Long userPowerUpId) {
        log.debug("User {} activating power-up {}", userId, userPowerUpId);

        UserPowerUp userPowerUp = userPowerUpRepository.findById(userPowerUpId)
                .orElseThrow(() -> new ResourceNotFoundException("UserPowerUp", "id", userPowerUpId));

        // Verify ownership
        if (!userPowerUp.getUser().getId().equals(userId)) {
            throw new InvalidOperationException("You don't own this power-up");
        }

        if (userPowerUp.getIsUsed()) {
            throw new InvalidOperationException("This power-up has already been used");
        }

        if (userPowerUp.getIsActive()) {
            throw new InvalidOperationException("This power-up is already active");
        }

        // Activate power-up
        userPowerUp.setIsActive(true);
        userPowerUp.setActivatedAt(LocalDateTime.now());

        // Set expiry if duration is specified
        if (userPowerUp.getPowerUp().getDurationHours() != null) {
            userPowerUp.setExpiresAt(LocalDateTime.now()
                    .plusHours(userPowerUp.getPowerUp().getDurationHours()));
        }

        // For instant power-ups (like heart refill), mark as used immediately
        if (userPowerUp.getPowerUp().getDurationHours() == null) {
            userPowerUp.setIsUsed(true);
            applyInstantPowerUp(userId, userPowerUp.getPowerUp().getType());
        }

        userPowerUp = userPowerUpRepository.save(userPowerUp);

        log.info("User {} activated power-up {} successfully", userId, userPowerUpId);

        return UserPowerUpResponse.from(userPowerUp);
    }

    /**
     * Apply instant power-up effects
     */
    private void applyInstantPowerUp(Long userId, PowerUp.Type type) {
        User user = userService.getUserById(userId);

        switch (type) {
            case HEART_REFILL:
                user.setHearts(user.getMaxHearts());
                log.info("Refilled hearts for user {}", userId);
                break;
            case XP_BOOST:
                // XP boost is handled during point calculation
                break;
            case STREAK_FREEZE:
                // Streak freeze is handled during streak calculation
                break;
            case UNLIMITED_HEARTS:
                // Unlimited hearts is checked during heart deduction
                break;
            default:
                log.warn("Unknown instant power-up type: {}", type);
        }
    }

    /**
     * Get user's power-ups
     */
    @Transactional(readOnly = true)
    public List<UserPowerUpResponse> getUserPowerUps(Long userId) {
        log.debug("Fetching power-ups for user id: {}", userId);

        userService.getUserById(userId);

        List<UserPowerUp> userPowerUps = userPowerUpRepository.findByUserIdOrderByPurchasedAtDesc(userId);

        return userPowerUps.stream()
                .map(UserPowerUpResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Get user's power-ups with pagination
     */
    @Transactional(readOnly = true)
    public Page<UserPowerUpResponse> getUserPowerUpsPaged(Long userId, Pageable pageable) {
        log.debug("Fetching paged power-ups for user id: {}", userId);

        userService.getUserById(userId);

        Page<UserPowerUp> userPowerUps = userPowerUpRepository.findByUserId(userId, pageable);

        return userPowerUps.map(UserPowerUpResponse::from);
    }

    /**
     * Get user's active power-ups
     */
    @Transactional(readOnly = true)
    public List<UserPowerUpResponse> getUserActivePowerUps(Long userId) {
        log.debug("Fetching active power-ups for user id: {}", userId);

        userService.getUserById(userId);

        List<UserPowerUp> activePowerUps = userPowerUpRepository
                .findCurrentlyActive(userId, LocalDateTime.now());

        return activePowerUps.stream()
                .map(UserPowerUpResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Check if user has active power-up of specific type
     */
    @Transactional(readOnly = true)
    public boolean hasActivePowerUp(Long userId, PowerUp.Type type) {
        List<UserPowerUp> activePowerUps = userPowerUpRepository
                .findCurrentlyActive(userId, LocalDateTime.now());

        return activePowerUps.stream()
                .anyMatch(up -> up.getPowerUp().getType() == type);
    }

    /**
     * Clean up expired power-ups
     */
    @Transactional
    public void cleanupExpiredPowerUps() {
        log.debug("Cleaning up expired power-ups");

        List<UserPowerUp> expiredPowerUps = userPowerUpRepository.findExpiredActives(LocalDateTime.now());

        for (UserPowerUp userPowerUp : expiredPowerUps) {
            userPowerUp.setIsActive(false);
            userPowerUp.setIsUsed(true);
            userPowerUpRepository.save(userPowerUp);
        }

        log.info("Cleaned up {} expired power-ups", expiredPowerUps.size());
    }

    /**
     * Get power-up count
     */
    @Transactional(readOnly = true)
    public long getTotalPowerUpCount() {
        return powerUpRepository.count();
    }

    /**
     * Get active power-up count
     */
    @Transactional(readOnly = true)
    public long getActivePowerUpCount() {
        return powerUpRepository.findAllByIsActiveTrue().size();
    }
}
