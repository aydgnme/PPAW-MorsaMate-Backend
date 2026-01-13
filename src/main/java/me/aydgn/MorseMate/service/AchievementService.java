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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<AchievementResponse> getAllAchievements() {
        log.debug("Fetching all achievements");
        List<Achievement> achievements = achievementRepository.findAllByOrderByIdAsc();
        return achievements.stream()
                .map(AchievementResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<AchievementResponse> getAllAchievementsPaged(Pageable pageable) {
        log.debug("Fetching paged achievements");
        Page<Achievement> achievements = achievementRepository.findAll(pageable);
        return achievements.map(AchievementResponse::from);
    }

    @Transactional(readOnly = true)
    public AchievementResponse getAchievementById(Long id) {
        log.debug("Fetching achievement with id: {}", id);
        Achievement achievement = findAchievementById(id);
        return AchievementResponse.from(achievement);
    }

    @Transactional(readOnly = true)
    public Achievement findAchievementById(Long id) {
        return achievementRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Achievement not found with id: {}", id);
                    return new ResourceNotFoundException("Achievement", "id", id);
                });
    }

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

    @Transactional
    public AchievementResponse createAchievement(CreateAchievementRequest request) {
        log.debug("Creating new achievement: {}", request.getName());

        if (achievementRepository.existsByNameIgnoreCase(request.getName())) {
            log.error("Achievement already exists with name: {}", request.getName());
            throw new InvalidOperationException(
                    String.format("Achievement already exists with name: %s", request.getName()));
        }

        Achievement achievement = Achievement.builder()
                .name(request.getName())
                .description(request.getDescription())
                .iconUrl(request.getIconUrl())
                .category(parseCategory(request.getCategory()))
                .rarity(parseRarity(request.getRarity()))
                .criteriaType(parseCriteriaType(request.getCriteriaType()))
                .criteriaTarget(request.getCriteriaTarget() != null ? request.getCriteriaTarget() : 1)
                .criteriaMetadata(request.getCriteriaMetadata())
                .points(request.getPoints() != null ? request.getPoints() : 10)
                .gemReward(request.getGemReward() != null ? request.getGemReward() : 0)
                .isActive(true)
                .build();

        achievement = achievementRepository.save(achievement);
        log.info("Achievement created successfully with id: {}", achievement.getId());

        return AchievementResponse.from(achievement);
    }

    @Transactional
    public AchievementResponse updateAchievement(Long id, UpdateAchievementRequest request) {
        log.debug("Updating achievement with id: {}", id);

        Achievement achievement = findAchievementById(id);

        if (request.getName() != null && !request.getName().isEmpty()) {
            if (!achievement.getName().equalsIgnoreCase(request.getName()) &&
                    achievementRepository.existsByNameIgnoreCase(request.getName())) {
                log.error("Achievement already exists with name: {}", request.getName());
                throw new InvalidOperationException(
                        String.format("Achievement already exists with name: %s", request.getName()));
            }
            achievement.setName(request.getName());
        }

        if (request.getDescription() != null)
            achievement.setDescription(request.getDescription());
        if (request.getIconUrl() != null)
            achievement.setIconUrl(request.getIconUrl());
        if (request.getCategory() != null)
            achievement.setCategory(parseCategory(request.getCategory()));
        if (request.getRarity() != null)
            achievement.setRarity(parseRarity(request.getRarity()));
        if (request.getCriteriaType() != null)
            achievement.setCriteriaType(parseCriteriaType(request.getCriteriaType()));
        if (request.getCriteriaTarget() != null)
            achievement.setCriteriaTarget(request.getCriteriaTarget());
        if (request.getCriteriaMetadata() != null)
            achievement.setCriteriaMetadata(request.getCriteriaMetadata());
        if (request.getPoints() != null)
            achievement.setPoints(request.getPoints());
        if (request.getGemReward() != null)
            achievement.setGemReward(request.getGemReward());

        achievement = achievementRepository.save(achievement);
        log.info("Achievement updated successfully with id: {}", id);

        return AchievementResponse.from(achievement);
    }

    @Transactional
    public void deleteAchievement(Long id) {
        log.debug("Deleting achievement with id: {}", id);

        Achievement achievement = findAchievementById(id);

        long usersWithAchievement = userAchievementRepository.countByAchievementId(id);
        if (usersWithAchievement > 0) {
            log.warn("Attempting to delete achievement {} that has been earned by {} users", id, usersWithAchievement);
            throw new InvalidOperationException(
                    String.format("Cannot delete achievement. It has been earned by %d user(s)", usersWithAchievement));
        }

        achievementRepository.delete(achievement);
        log.info("Achievement deleted successfully with id: {}", id);
    }

    @Transactional
    public void awardAchievementToUser(Long userId, Long achievementId) {
        log.debug("Awarding achievement {} to user {}", achievementId, userId);

        User user = userService.getUserById(userId);
        Achievement achievement = findAchievementById(achievementId);

        if (userAchievementRepository.existsByUserIdAndAchievementId(userId, achievementId)) {
            log.warn("User {} already has achievement {}", userId, achievementId);
            return;
        }

        UserAchievement userAchievement = UserAchievement.builder()
                .user(user)
                .achievement(achievement)
                .isUnlocked(true)
                .unlockedAt(LocalDateTime.now())
                .currentProgress(achievement.getCriteriaTarget())
                .build();

        userAchievementRepository.save(userAchievement);

        user.setTotalPoints(user.getTotalPoints() + achievement.getPoints());

        if (achievement.getGemReward() > 0) {
            log.info("Achievement {} awards {} gems to user {}", achievementId, achievement.getGemReward(), userId);
        }

        log.info("Achievement {} awarded to user {} successfully", achievementId, userId);
    }

    @Transactional(readOnly = true)
    public List<AchievementResponse> getUserAchievements(Long userId) {
        log.debug("Fetching achievements for user id: {}", userId);
        userService.getUserById(userId);

        List<UserAchievement> userAchievements = userAchievementRepository
                .findByUserIdOrderByUnlockedAtDesc(userId);

        return userAchievements.stream()
                .map(ua -> {
                    AchievementResponse response = AchievementResponse.from(ua.getAchievement());
                    response.setUnlockedAt(ua.getUnlockedAt());
                    response.setEarned(true);
                    response.setCurrentProgress(ua.getCurrentProgress());
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<AchievementResponse> getUserAchievementsPaged(Long userId, Pageable pageable) {
        log.debug("Fetching paged achievements for user id: {}", userId);
        userService.getUserById(userId);

        Page<UserAchievement> userAchievements = userAchievementRepository.findByUserId(userId, pageable);

        return userAchievements.map(ua -> {
            AchievementResponse response = AchievementResponse.from(ua.getAchievement());
            response.setUnlockedAt(ua.getUnlockedAt());
            response.setEarned(true);
            response.setCurrentProgress(ua.getCurrentProgress());
            return response;
        });
    }

    @Transactional(readOnly = true)
    public boolean hasUserEarnedAchievement(Long userId, Long achievementId) {
        return userAchievementRepository.existsByUserIdAndAchievementId(userId, achievementId);
    }

    @Transactional(readOnly = true)
    public long getUserAchievementCount(Long userId) {
        log.debug("Counting achievements for user id: {}", userId);
        userService.getUserById(userId);
        return userAchievementRepository.countByUserId(userId);
    }

    @Transactional(readOnly = true)
    public long getTotalAchievementCount() {
        return achievementRepository.count();
    }

    public void checkAndAwardAchievements(Long userId) {
        log.debug("Checking achievement criteria for user: {}", userId);
        User user = userService.getUserById(userId);
        List<Achievement> activeAchievements = achievementRepository.findAll();

        for (Achievement achievement : activeAchievements) {
            if (!achievement.getIsActive())
                continue;
            if (hasUserEarnedAchievement(userId, achievement.getId()))
                continue;

            if (checkAchievementCriteria(user, achievement)) {
                awardAchievementToUser(userId, achievement.getId());
            }
        }
    }

    private boolean checkAchievementCriteria(User user, Achievement achievement) {
        Achievement.CriteriaType type = achievement.getCriteriaType();
        int target = achievement.getCriteriaTarget();

        if (type == null)
            return false;

        switch (type) {
            case STREAK_DAYS:
                return user.getCurrentStreak() >= target;
            case TOTAL_POINTS:
                return user.getTotalPoints() >= target;
            case LESSONS_COMPLETED:
                return false;
            default:
                return false;
        }
    }

    private Achievement.AchievementCategory parseCategory(String ignored) {
        if (ignored == null)
            return Achievement.AchievementCategory.PROGRESS;
        try {
            return Achievement.AchievementCategory.valueOf(ignored.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Achievement.AchievementCategory.PROGRESS;
        }
    }

    private Achievement.AchievementRarity parseRarity(String val) {
        if (val == null)
            return Achievement.AchievementRarity.COMMON;
        try {
            return Achievement.AchievementRarity.valueOf(val.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Achievement.AchievementRarity.COMMON;
        }
    }

    private Achievement.CriteriaType parseCriteriaType(String val) {
        if (val == null)
            return Achievement.CriteriaType.LESSONS_COMPLETED;
        try {
            return Achievement.CriteriaType.valueOf(val.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Achievement.CriteriaType.LESSONS_COMPLETED;
        }
    }
}
