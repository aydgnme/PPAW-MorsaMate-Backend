package me.aydgn.MorseMate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.dto.response.UserProgressResponse;
import me.aydgn.MorseMate.entity.Lesson;
import me.aydgn.MorseMate.entity.User;
import me.aydgn.MorseMate.entity.UserProgress;
import me.aydgn.MorseMate.exception.ResourceNotFoundException;
import me.aydgn.MorseMate.repository.UserProgressRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProgressService {

    private final UserProgressRepository userProgressRepository;
    private final UserService userService;
    private final LessonService lessonService;
    private final GemService gemService;
    private final AchievementService achievementService;

    /**
     * Get or create user progress for a lesson
     */
    @Transactional
    public UserProgress getOrCreateProgress(Long userId, Long lessonId) {
        log.debug("Getting or creating progress for user id: {} on lesson id: {}", userId, lessonId);

        User user = userService.getUserById(userId);
        Lesson lesson = lessonService.findLessonById(lessonId);

        Optional<UserProgress> existing = userProgressRepository.findByUserIdAndLessonId(userId, lessonId);
        if (existing.isPresent()) {
            return existing.get();
        }

        // Create new progress record
        UserProgress progress = UserProgress.builder()
                .user(user)
                .lesson(lesson)
                .attempts(0)
                .isCompleted(false)
                .build();

        progress = userProgressRepository.save(progress);
        log.info("Created new progress record with id: {} for user {} on lesson {}",
                progress.getId(), userId, lessonId);

        return progress;
    }

    /**
     * Get user progress for a specific lesson
     */
    @Transactional(readOnly = true)
    public UserProgressResponse getUserProgressForLesson(Long userId, Long lessonId) {
        log.debug("Fetching progress for user id: {} on lesson id: {}", userId, lessonId);

        // Verify user and lesson exist
        userService.getUserById(userId);
        lessonService.findLessonById(lessonId);

        UserProgress progress = userProgressRepository.findByUserIdAndLessonId(userId, lessonId)
                .orElseThrow(() -> {
                    log.error("No progress found for user id: {} on lesson id: {}", userId, lessonId);
                    return new ResourceNotFoundException("UserProgress", "userId-lessonId", userId + "-" + lessonId);
                });

        return UserProgressResponse.from(progress);
    }

    /**
     * Get all progress records for a user
     */
    @Transactional(readOnly = true)
    public List<UserProgressResponse> getAllUserProgress(Long userId) {
        log.debug("Fetching all progress for user id: {}", userId);
        // Verify user exists
        userService.getUserById(userId);

        List<UserProgress> progressList = userProgressRepository.findByUserIdOrderByCompletedAtDesc(userId);
        return progressList.stream()
                .map(UserProgressResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Get user progress with pagination
     */
    @Transactional(readOnly = true)
    public Page<UserProgressResponse> getAllUserProgressPaged(Long userId, Pageable pageable) {
        log.debug("Fetching paged progress for user id: {}", userId);
        // Verify user exists
        userService.getUserById(userId);

        Page<UserProgress> progressPage = userProgressRepository.findByUserId(userId, pageable);
        return progressPage.map(UserProgressResponse::from);
    }

    /**
     * Get progress for a specific lesson (all users)
     */
    @Transactional(readOnly = true)
    public List<UserProgressResponse> getLessonProgress(Long lessonId) {
        log.debug("Fetching progress for lesson id: {}", lessonId);
        // Verify lesson exists
        lessonService.findLessonById(lessonId);

        List<UserProgress> progressList = userProgressRepository.findByLessonId(lessonId);
        return progressList.stream()
                .map(UserProgressResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Increment attempt count for a lesson
     */
    @Transactional
    public UserProgressResponse incrementAttempts(Long userId, Long lessonId) {
        log.debug("Incrementing attempts for user id: {} on lesson id: {}", userId, lessonId);

        UserProgress progress = getOrCreateProgress(userId, lessonId);
        progress.incrementAttempts();
        progress = userProgressRepository.save(progress);

        log.info("Incremented attempts for progress id: {} (new count: {})",
                progress.getId(), progress.getAttempts());

        return UserProgressResponse.from(progress);
    }

    /**
     * Mark a lesson as completed
     */
    @Transactional
    public UserProgressResponse markLessonCompleted(Long userId, Long lessonId, Integer score, Integer stars, Integer timeSpent) {
        log.debug("Marking lesson completed for user id: {} on lesson id: {}", userId, lessonId);

        UserProgress progress = getOrCreateProgress(userId, lessonId);
        Lesson lesson = progress.getLesson();
        boolean wasAlreadyCompleted = progress.getIsCompleted();

        progress.markCompleted(LocalDateTime.now(), score, stars, timeSpent);
        progress = userProgressRepository.save(progress);

        log.info("Marked lesson completed for progress id: {} (score: {}, stars: {}, time: {}s)",
                progress.getId(), score, stars, timeSpent);

        // Award gems for first-time lesson completion
        if (!wasAlreadyCompleted) {
            int gemsEarned = calculateLessonGems(stars);
            if (gemsEarned > 0) {
                try {
                    gemService.addGems(userId, gemsEarned,
                        "Lesson Completion",
                        "Completed lesson: " + lesson.getTitle());
                    log.info("Awarded {} gems to user {} for completing lesson {}", gemsEarned, userId, lessonId);
                } catch (Exception e) {
                    log.warn("Failed to award gems to user {}: {}", userId, e.getMessage());
                }
            }

            // Check and award achievements
            try {
                achievementService.checkAndAwardAchievements(userId);
                log.debug("Checked achievements for user {}", userId);
            } catch (Exception e) {
                log.warn("Failed to check achievements for user {}: {}", userId, e.getMessage());
            }
        }

        return UserProgressResponse.from(progress);
    }

    /**
     * Calculate gems earned based on stars
     */
    private int calculateLessonGems(Integer stars) {
        if (stars == null) return 10;
        return switch (stars) {
            case 3 -> 30;
            case 2 -> 20;
            case 1 -> 10;
            default -> 10;
        };
    }

    /**
     * Reset lesson completion (for retrying)
     */
    @Transactional
    public UserProgressResponse resetLessonCompletion(Long userId, Long lessonId) {
        log.debug("Resetting lesson completion for user id: {} on lesson id: {}", userId, lessonId);

        // Verify progress exists
        UserProgress progress = userProgressRepository.findByUserIdAndLessonId(userId, lessonId)
                .orElseThrow(() -> {
                    log.error("No progress found for user id: {} on lesson id: {}", userId, lessonId);
                    return new ResourceNotFoundException("UserProgress", "userId-lessonId", userId + "-" + lessonId);
                });

        int updated = userProgressRepository.resetCompletion(userId, lessonId);
        if (updated > 0) {
            // Refresh the entity
            progress = userProgressRepository.findByUserIdAndLessonId(userId, lessonId).orElseThrow();
            log.info("Reset completion for progress id: {}", progress.getId());
        }

        return UserProgressResponse.from(progress);
    }

    /**
     * Get overall user statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getUserStatistics(Long userId) {
        log.debug("Calculating overall statistics for user id: {}", userId);
        // Verify user exists
        userService.getUserById(userId);

        long totalLessons = userProgressRepository.countByUserIdAndIsCompletedTrue(userId);
        List<UserProgress> allProgress = userProgressRepository.findByUserIdOrderByCompletedAtDesc(userId);

        int totalAttempts = allProgress.stream()
                .mapToInt(UserProgress::getAttempts)
                .sum();

        int totalScore = allProgress.stream()
                .filter(p -> p.getScore() != null)
                .mapToInt(UserProgress::getScore)
                .sum();

        double averageScore = allProgress.stream()
                .filter(p -> p.getScore() != null)
                .mapToInt(UserProgress::getScore)
                .average()
                .orElse(0.0);

        int totalStars = allProgress.stream()
                .filter(p -> p.getStarsEarned() != null)
                .mapToInt(UserProgress::getStarsEarned)
                .sum();

        int totalTimeSpent = allProgress.stream()
                .filter(p -> p.getTimeSpent() != null)
                .mapToInt(UserProgress::getTimeSpent)
                .sum();

        Map<String, Object> stats = new HashMap<>();
        stats.put("completedLessons", totalLessons);
        stats.put("totalAttempts", totalAttempts);
        stats.put("totalScore", totalScore);
        stats.put("averageScore", averageScore);
        stats.put("totalStars", totalStars);
        stats.put("totalTimeSpentSeconds", totalTimeSpent);
        stats.put("totalLessonsStarted", allProgress.size());

        return stats;
    }

    /**
     * Get progress statistics for a lesson (all users)
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getLessonStatistics(Long lessonId) {
        log.debug("Calculating statistics for lesson id: {}", lessonId);
        // Verify lesson exists
        lessonService.findLessonById(lessonId);

        long completedCount = userProgressRepository.countByLessonIdAndIsCompletedTrue(lessonId);
        Double averageScore = userProgressRepository.averageScoreForLesson(lessonId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("completedCount", completedCount);
        stats.put("averageScore", averageScore != null ? averageScore : 0.0);

        return stats;
    }

    /**
     * Check if user has completed a lesson
     */
    @Transactional(readOnly = true)
    public boolean hasCompletedLesson(Long userId, Long lessonId) {
        log.debug("Checking if user id: {} has completed lesson id: {}", userId, lessonId);

        Optional<UserProgress> progress = userProgressRepository.findByUserIdAndLessonId(userId, lessonId);
        return progress.map(UserProgress::getIsCompleted).orElse(false);
    }

    /**
     * Get completed lesson count for a user
     */
    @Transactional(readOnly = true)
    public long getCompletedLessonCount(Long userId) {
        log.debug("Counting completed lessons for user id: {}", userId);
        // Verify user exists
        userService.getUserById(userId);

        return userProgressRepository.countByUserIdAndIsCompletedTrue(userId);
    }

    /**
     * Update progress (used after exercise attempts)
     */
    @Transactional
    public UserProgressResponse updateProgress(Long userId, Long lessonId) {
        log.debug("Updating progress for user id: {} on lesson id: {}", userId, lessonId);

        UserProgress progress = getOrCreateProgress(userId, lessonId);
        progress.incrementAttempts();
        progress = userProgressRepository.save(progress);

        log.info("Updated progress for progress id: {}", progress.getId());

        return UserProgressResponse.from(progress);
    }
}
