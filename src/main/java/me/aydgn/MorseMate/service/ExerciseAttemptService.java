package me.aydgn.MorseMate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.dto.request.CreateExerciseAttemptRequest;
import me.aydgn.MorseMate.dto.response.ExerciseAttemptResponse;
import me.aydgn.MorseMate.entity.Exercise;
import me.aydgn.MorseMate.entity.ExerciseAttempt;
import me.aydgn.MorseMate.entity.User;
import me.aydgn.MorseMate.exception.ResourceNotFoundException;
import me.aydgn.MorseMate.repository.ExerciseAttemptRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExerciseAttemptService {

    private final ExerciseAttemptRepository exerciseAttemptRepository;
    private final ExerciseService exerciseService;
    private final UserService userService;

    /**
     * Record an exercise attempt
     */
    @Transactional
    public ExerciseAttemptResponse recordAttempt(Long userId, CreateExerciseAttemptRequest request) {
        log.debug("Recording attempt for user id: {} on exercise id: {}", userId, request.getExerciseId());

        // Verify user and exercise exist
        User user = userService.getUserById(userId);
        Exercise exercise = exerciseService.findExerciseById(request.getExerciseId());

        // Validate the answer
        boolean isCorrect = exerciseService.validateAnswer(request.getUserAnswer(), exercise.getCorrectAnswer());

        // Calculate points earned
        int pointsEarned = 0;
        if (isCorrect) {
            pointsEarned = exercise.getPoints();
            // Bonus points for fast completion within time limit
            if (exercise.getTimeLimit() != null && request.getTimeTaken() != null) {
                if (request.getTimeTaken() < exercise.getTimeLimit()) {
                    int timeBonus = (int) ((exercise.getTimeLimit() - request.getTimeTaken()) * 0.1);
                    pointsEarned += timeBonus;
                }
            }
        }

        // Create attempt record
        ExerciseAttempt attempt = ExerciseAttempt.builder()
                .user(user)
                .exercise(exercise)
                .userAnswer(request.getUserAnswer())
                .isCorrect(isCorrect)
                .timeTaken(request.getTimeTaken())
                .pointsEarned(pointsEarned)
                .build();

        attempt = exerciseAttemptRepository.save(attempt);
        log.info("Exercise attempt recorded with id: {} (correct: {}, points: {})",
                attempt.getId(), isCorrect, pointsEarned);

        return ExerciseAttemptResponse.from(attempt);
    }

    /**
     * Get all attempts for a user
     */
    @Transactional(readOnly = true)
    public List<ExerciseAttemptResponse> getUserAttempts(Long userId) {
        log.debug("Fetching attempts for user id: {}", userId);
        // Verify user exists
        userService.getUserById(userId);

        List<ExerciseAttempt> attempts = exerciseAttemptRepository.findByUserIdOrderByAttemptedAtDesc(userId);
        return attempts.stream()
                .map(ExerciseAttemptResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Get attempts for a user with pagination
     */
    @Transactional(readOnly = true)
    public Page<ExerciseAttemptResponse> getUserAttemptsPaged(Long userId, Pageable pageable) {
        log.debug("Fetching paged attempts for user id: {}", userId);
        // Verify user exists
        userService.getUserById(userId);

        Page<ExerciseAttempt> attempts = exerciseAttemptRepository.findByUserId(userId, pageable);
        return attempts.map(ExerciseAttemptResponse::from);
    }

    /**
     * Get attempts for a specific exercise by a user
     */
    @Transactional(readOnly = true)
    public List<ExerciseAttemptResponse> getUserExerciseHistory(Long userId, Long exerciseId) {
        log.debug("Fetching attempt history for user id: {} on exercise id: {}", userId, exerciseId);
        // Verify user and exercise exist
        userService.getUserById(userId);
        exerciseService.findExerciseById(exerciseId);

        List<ExerciseAttempt> attempts = exerciseAttemptRepository
                .findByUserIdAndExerciseIdOrderByAttemptedAtDesc(userId, exerciseId);
        return attempts.stream()
                .map(ExerciseAttemptResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Get attempts within a time range
     */
    @Transactional(readOnly = true)
    public List<ExerciseAttemptResponse> getUserAttemptsInRange(Long userId, LocalDateTime from, LocalDateTime to) {
        log.debug("Fetching attempts for user id: {} from {} to {}", userId, from, to);
        // Verify user exists
        userService.getUserById(userId);

        List<ExerciseAttempt> attempts = exerciseAttemptRepository
                .findByUserIdAndAttemptedAtBetweenOrderByAttemptedAtDesc(userId, from, to);
        return attempts.stream()
                .map(ExerciseAttemptResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Get statistics for a user's exercise attempts
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getUserExerciseStatistics(Long userId, Long exerciseId) {
        log.debug("Calculating statistics for user id: {} on exercise id: {}", userId, exerciseId);
        // Verify user and exercise exist
        userService.getUserById(userId);
        exerciseService.findExerciseById(exerciseId);

        long totalAttempts = exerciseAttemptRepository.countByUserIdAndExerciseId(userId, exerciseId);
        long correctAttempts = exerciseAttemptRepository.countByUserIdAndExerciseIdAndIsCorrectTrue(userId, exerciseId);
        Double averageTime = exerciseAttemptRepository.averageTimeForUserAndExercise(userId, exerciseId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAttempts", totalAttempts);
        stats.put("correctAttempts", correctAttempts);
        stats.put("incorrectAttempts", totalAttempts - correctAttempts);
        stats.put("successRate", totalAttempts > 0 ? (double) correctAttempts / totalAttempts * 100 : 0.0);
        stats.put("averageTimeSeconds", averageTime != null ? averageTime : 0.0);

        log.info("Statistics for user {} on exercise {}: {} attempts, {:.1f}% success rate",
                userId, exerciseId, totalAttempts, (double) correctAttempts / totalAttempts * 100);

        return stats;
    }

    /**
     * Get overall user statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getUserStatistics(Long userId) {
        log.debug("Calculating overall statistics for user id: {}", userId);
        // Verify user exists
        userService.getUserById(userId);

        long totalAttempts = exerciseAttemptRepository.countByUserId(userId);
        long correctAttempts = exerciseAttemptRepository.countByUserIdAndIsCorrectTrue(userId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAttempts", totalAttempts);
        stats.put("correctAttempts", correctAttempts);
        stats.put("incorrectAttempts", totalAttempts - correctAttempts);
        stats.put("successRate", totalAttempts > 0 ? (double) correctAttempts / totalAttempts * 100 : 0.0);

        return stats;
    }

    /**
     * Get correctness rate for a user in a time range
     */
    @Transactional(readOnly = true)
    public double getCorrectnessRate(Long userId, LocalDateTime from, LocalDateTime to) {
        log.debug("Calculating correctness rate for user id: {} from {} to {}", userId, from, to);
        // Verify user exists
        userService.getUserById(userId);

        Double rate = exerciseAttemptRepository.correctnessRateInRange(userId, from, to);
        return rate != null ? rate * 100 : 0.0; // Convert to percentage
    }

    /**
     * Get the last attempt for a user on an exercise
     */
    @Transactional(readOnly = true)
    public ExerciseAttemptResponse getLastAttempt(Long userId, Long exerciseId) {
        log.debug("Fetching last attempt for user id: {} on exercise id: {}", userId, exerciseId);
        // Verify user and exercise exist
        userService.getUserById(userId);
        exerciseService.findExerciseById(exerciseId);

        ExerciseAttempt attempt = exerciseAttemptRepository
                .findFirstByUserIdAndExerciseIdOrderByAttemptedAtDesc(userId, exerciseId)
                .orElseThrow(() -> {
                    log.error("No attempts found for user id: {} on exercise id: {}", userId, exerciseId);
                    return new ResourceNotFoundException("ExerciseAttempt", "userId-exerciseId", userId + "-" + exerciseId);
                });

        return ExerciseAttemptResponse.from(attempt);
    }

    /**
     * Calculate accuracy percentage between two strings
     */
    public double calculateAccuracy(String userAnswer, String correctAnswer) {
        return exerciseService.calculateAccuracy(userAnswer, correctAnswer);
    }
}
