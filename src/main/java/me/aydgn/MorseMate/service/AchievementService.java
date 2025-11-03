package me.aydgn.MorseMate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.dto.request.CreateAchievementRequest;
import me.aydgn.MorseMate.dto.request.UpdateAchievementRequest;
import me.aydgn.MorseMate.dto.response.AchievementResponse;
import me.aydgn.MorseMate.entity.Achievement;
import me.aydgn.MorseMate.entity.User;
import me.aydgn.MorseMate.entity.UserAchievement;
import me.aydgn.MorseMate.exception.InvalidOperationException;
import me.aydgn.MorseMate.exception.ResourceNotFoundException;
import me.aydgn.MorseMate.repository.AchievementRepository;
import me.aydgn.MorseMate.repository.UserAchievementRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final UserService userService;

    /**
     * Get all achievements
     */
    @Transactional(readOnly = true)
    public List<AchievementResponse> getAllAchievements() {
        log.debug("Fetching all achievements");
        List<Achievement> achievements = achievementRepository.findAllByOrderByIdAsc();
        return achievements.stream()
                .map(AchievementResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Get all achievements with pagination
     */
    @Transactional(readOnly = true)
    public Page<AchievementResponse> getAllAchievementsPaged(Pageable pageable) {
        log.debug("Fetching paged achievements");
        Page<Achievement> achievements = achievementRepository.findAll(pageable);
        return achievements.map(AchievementResponse::from);
    }

    /**
     * Get achievement by ID
     */
    @Transactional(readOnly = true)
    public AchievementResponse getAchievementById(Long id) {
        log.debug("Fetching achievement with id: {}", id);
        Achievement achievement = findAchievementById(id);
        return AchievementResponse.from(achievement);
    }

    /**
     * Get achievement entity by ID (internal use)
     */
    @Transactional(readOnly = true)
    public Achievement findAchievementById(Long id) {
        return achievementRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Achievement not found with id: {}", id);
                    return new ResourceNotFoundException("Achievement", "id", id);
                });
    }

    /**
     * Get achievement by name
     */
    @Transactional(readOnly = true)
    public AchievementResponse getAchievementByName(String name) {
        log.debug("Fetching achievement with name: {}", name);
        Achievement achievement = achievementRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> {
                    log.error("Achievement not found with name: {}", name);
                    return new ResourceNotFoundException("Achievement", "name", name);
                });
        return AchievementResponse.from(achievement);
    }

    /**
     * Create a new achievement
     */
    @Transactional
    public AchievementResponse createAchievement(CreateAchievementRequest request) {
        log.debug("Creating new achievement: {}", request.getName());

        // Check if achievement with same name already exists
        if (achievementRepository.existsByNameIgnoreCase(request.getName())) {
            log.error("Achievement already exists with name: {}", request.getName());
            throw new InvalidOperationException(
                    String.format("Achievement already exists with name: %s", request.getName())
            );
        }

        Achievement achievement = Achievement.builder()
                .name(request.getName())
                .description(request.getDescription())
                .icon(request.getIcon())
                .criteria(request.getCriteria())
                .points(request.getPoints() != null ? request.getPoints() : 10)
                .gemReward(request.getGemReward() != null ? request.getGemReward() : 0)
                .build();

        achievement = achievementRepository.save(achievement);
        log.info("Achievement created successfully with id: {}", achievement.getId());

        return AchievementResponse.from(achievement);
    }

    /**
     * Update an existing achievement
     */
    @Transactional
    public AchievementResponse updateAchievement(Long id, UpdateAchievementRequest request) {
        log.debug("Updating achievement with id: {}", id);

        Achievement achievement = findAchievementById(id);

        // Update name if provided and check uniqueness
        if (request.getName() != null && !request.getName().isEmpty()) {
            if (!achievement.getName().equalsIgnoreCase(request.getName()) &&
                    achievementRepository.existsByNameIgnoreCase(request.getName())) {
                log.error("Achievement already exists with name: {}", request.getName());
                throw new InvalidOperationException(
                        String.format("Achievement already exists with name: %s", request.getName())
                );
            }
            achievement.setName(request.getName());
        }

        // Update description if provided
        if (request.getDescription() != null) {
            achievement.setDescription(request.getDescription());
        }

        // Update icon if provided
        if (request.getIcon() != null) {
            achievement.setIcon(request.getIcon());
        }

        // Update criteria if provided
        if (request.getCriteria() != null) {
            achievement.setCriteria(request.getCriteria());
        }

        // Update points if provided
        if (request.getPoints() != null) {
            achievement.setPoints(request.getPoints());
        }

        // Update gem reward if provided
        if (request.getGemReward() != null) {
            achievement.setGemReward(request.getGemReward());
        }

        achievement = achievementRepository.save(achievement);
        log.info("Achievement updated successfully with id: {}", id);

        return AchievementResponse.from(achievement);
    }

    /**
     * Delete an achievement
     */
    @Transactional
    public void deleteAchievement(Long id) {
        log.debug("Deleting achievement with id: {}", id);

        Achievement achievement = findAchievementById(id);

        // Check if any users have earned this achievement
        long usersWithAchievement = userAchievementRepository.countByAchievementId(id);
        if (usersWithAchievement > 0) {
            log.warn("Attempting to delete achievement {} that has been earned by {} users", id, usersWithAchievement);
            throw new InvalidOperationException(
                    String.format("Cannot delete achievement. It has been earned by %d user(s)", usersWithAchievement)
            );
        }

        achievementRepository.delete(achievement);
        log.info("Achievement deleted successfully with id: {}", id);
    }

    /**
     * Award achievement to user
     */
    @Transactional
    public void awardAchievementToUser(Long userId, Long achievementId) {
        log.debug("Awarding achievement {} to user {}", achievementId, userId);

        User user = userService.getUserById(userId);
        Achievement achievement = findAchievementById(achievementId);

        // Check if user already has this achievement
        if (userAchievementRepository.existsByUserIdAndAchievementId(userId, achievementId)) {
            log.warn("User {} already has achievement {}", userId, achievementId);
            throw new InvalidOperationException(
                    String.format("User already has this achievement")
            );
        }

        // Create user achievement record
        UserAchievement userAchievement = UserAchievement.builder()
                .user(user)
                .achievement(achievement)
                .earnedAt(LocalDateTime.now())
                .build();

        userAchievementRepository.save(userAchievement);

        // Award points to user
        user.setTotalPoints(user.getTotalPoints() + achievement.getPoints());

        // Award gems if any
        if (achievement.getGemReward() > 0) {
            // Gem awarding will be handled by GemService in future
            log.info("Achievement {} awards {} gems to user {}", achievementId, achievement.getGemReward(), userId);
        }

        log.info("Achievement {} awarded to user {} successfully", achievementId, userId);
    }

    /**
     * Get user's achievements
     */
    @Transactional(readOnly = true)
    public List<AchievementResponse> getUserAchievements(Long userId) {
        log.debug("Fetching achievements for user id: {}", userId);

        // Verify user exists
        userService.getUserById(userId);

        List<UserAchievement> userAchievements = userAchievementRepository
                .findByUserIdOrderByEarnedAtDesc(userId);

        return userAchievements.stream()
                .map(ua -> {
                    AchievementResponse response = AchievementResponse.from(ua.getAchievement());
                    response.setEarnedAt(ua.getEarnedAt());
                    response.setEarned(true);
                    return response;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get user's achievements with pagination
     */
    @Transactional(readOnly = true)
    public Page<AchievementResponse> getUserAchievementsPaged(Long userId, Pageable pageable) {
        log.debug("Fetching paged achievements for user id: {}", userId);

        // Verify user exists
        userService.getUserById(userId);

        Page<UserAchievement> userAchievements = userAchievementRepository.findByUserId(userId, pageable);

        return userAchievements.map(ua -> {
            AchievementResponse response = AchievementResponse.from(ua.getAchievement());
            response.setEarnedAt(ua.getEarnedAt());
            response.setEarned(true);
            return response;
        });
    }

    /**
     * Check if user has specific achievement
     */
    @Transactional(readOnly = true)
    public boolean hasUserEarnedAchievement(Long userId, Long achievementId) {
        log.debug("Checking if user {} has achievement {}", userId, achievementId);
        return userAchievementRepository.existsByUserIdAndAchievementId(userId, achievementId);
    }

    /**
     * Get achievement count for user
     */
    @Transactional(readOnly = true)
    public long getUserAchievementCount(Long userId) {
        log.debug("Counting achievements for user id: {}", userId);

        // Verify user exists
        userService.getUserById(userId);

        return userAchievementRepository.countByUserId(userId);
    }

    /**
     * Get total achievement count
     */
    @Transactional(readOnly = true)
    public long getTotalAchievementCount() {
        return achievementRepository.count();
    }

    /**
     * Check achievement criteria for user
     * This is a placeholder for future implementation
     */
    public void checkAndAwardAchievements(Long userId) {
        log.debug("Checking achievement criteria for user: {}", userId);

        User user = userService.getUserById(userId);
        List<Achievement> allAchievements = achievementRepository.findAll();

        for (Achievement achievement : allAchievements) {
            // Skip if user already has this achievement
            if (hasUserEarnedAchievement(userId, achievement.getId())) {
                continue;
            }

            // Check criteria
            if (checkAchievementCriteria(user, achievement.getCriteria())) {
                awardAchievementToUser(userId, achievement.getId());
            }
        }
    }

    /**
     * Check if user meets achievement criteria
     * This is a placeholder for future implementation
     */
    private boolean checkAchievementCriteria(User user, Map<String, Object> criteria) {
        if (criteria == null || criteria.isEmpty()) {
            return false;
        }

        String type = (String) criteria.get("type");
        Object value = criteria.get("value");

        if (type == null || value == null) {
            return false;
        }

        switch (type) {
            case "streak":
                int requiredStreak = ((Number) value).intValue();
                return user.getCurrentStreak() >= requiredStreak;

            case "points":
                int requiredPoints = ((Number) value).intValue();
                return user.getTotalPoints() >= requiredPoints;

            case "level":
                int requiredLevel = ((Number) value).intValue();
                return user.getLevel() >= requiredLevel;

            case "lessons_completed":
                // This requires UserProgress service integration
                log.debug("Lessons completed criteria check not yet implemented");
                return false;

            case "exercises_completed":
                // This requires ExerciseAttempt service integration
                log.debug("Exercises completed criteria check not yet implemented");
                return false;

            default:
                log.warn("Unknown achievement criteria type: {}", type);
                return false;
        }
    }
}
