package me.aydgn.MorseMate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.dto.request.CreateExerciseRequest;
import me.aydgn.MorseMate.dto.request.UpdateExerciseRequest;
import me.aydgn.MorseMate.dto.response.ExerciseResponse;
import me.aydgn.MorseMate.entity.Exercise;
import me.aydgn.MorseMate.entity.Lesson;
import me.aydgn.MorseMate.exception.InvalidOperationException;
import me.aydgn.MorseMate.exception.ResourceNotFoundException;
import me.aydgn.MorseMate.repository.ExerciseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final LessonService lessonService;

    /**
     * Get all exercises for a specific lesson
     */
    @Transactional(readOnly = true)
    public List<ExerciseResponse> getExercisesByLessonId(Long lessonId) {
        log.debug("Fetching exercises for lesson id: {}", lessonId);
        // Verify lesson exists
        lessonService.findLessonById(lessonId);

        List<Exercise> exercises = exerciseRepository.findByLessonId(lessonId);
        return exercises.stream()
                .map(ex -> ExerciseResponse.from(ex, false))
                .collect(Collectors.toList());
    }

    /**
     * Get exercises by lesson and difficulty
     */
    @Transactional(readOnly = true)
    public List<ExerciseResponse> getExercisesByLessonAndDifficulty(Long lessonId, String difficulty) {
        log.debug("Fetching exercises for lesson id: {} with difficulty: {}", lessonId, difficulty);
        // Verify lesson exists
        lessonService.findLessonById(lessonId);

        Exercise.Difficulty difficultyEnum;
        try {
            difficultyEnum = Exercise.Difficulty.valueOf(difficulty.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("Invalid difficulty value: {}", difficulty);
            throw new InvalidOperationException(
                    String.format("Invalid difficulty value: %s. Valid values are: EASY, MEDIUM, HARD", difficulty)
            );
        }

        List<Exercise> exercises = exerciseRepository.findByLessonIdAndDifficulty(lessonId, difficultyEnum);
        return exercises.stream()
                .map(ex -> ExerciseResponse.from(ex, false))
                .collect(Collectors.toList());
    }

    /**
     * Get exercises by lesson and type
     */
    @Transactional(readOnly = true)
    public List<ExerciseResponse> getExercisesByLessonAndType(Long lessonId, String type) {
        log.debug("Fetching exercises for lesson id: {} with type: {}", lessonId, type);
        // Verify lesson exists
        lessonService.findLessonById(lessonId);

        Exercise.Type typeEnum;
        try {
            typeEnum = Exercise.Type.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("Invalid exercise type: {}", type);
            throw new InvalidOperationException(
                    String.format("Invalid exercise type: %s. Valid values are: ENCODE, DECODE, AUDIO, SPEED_TEST, MULTI_CHOICE", type)
            );
        }

        List<Exercise> exercises = exerciseRepository.findByLessonIdAndType(lessonId, typeEnum);
        return exercises.stream()
                .map(ex -> ExerciseResponse.from(ex, false))
                .collect(Collectors.toList());
    }

    /**
     * Get a random exercise for a lesson with optional filters
     */
    @Transactional(readOnly = true)
    public ExerciseResponse getRandomExercise(Long lessonId, String type, String difficulty) {
        log.debug("Fetching random exercise for lesson id: {} (type: {}, difficulty: {})", lessonId, type, difficulty);
        // Verify lesson exists
        lessonService.findLessonById(lessonId);

        Exercise.Type typeEnum = null;
        if (type != null && !type.isEmpty()) {
            try {
                typeEnum = Exercise.Type.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.error("Invalid exercise type: {}", type);
                throw new InvalidOperationException(
                        String.format("Invalid exercise type: %s. Valid values are: ENCODE, DECODE, AUDIO, SPEED_TEST, MULTI_CHOICE", type)
                );
            }
        }

        Exercise.Difficulty difficultyEnum = null;
        if (difficulty != null && !difficulty.isEmpty()) {
            try {
                difficultyEnum = Exercise.Difficulty.valueOf(difficulty.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.error("Invalid difficulty value: {}", difficulty);
                throw new InvalidOperationException(
                        String.format("Invalid difficulty value: %s. Valid values are: EASY, MEDIUM, HARD", difficulty)
                );
            }
        }

        Page<Exercise> randomExercises = exerciseRepository.findRandomByLesson(
                lessonId, typeEnum, difficultyEnum, PageRequest.of(0, 1)
        );

        if (randomExercises.isEmpty()) {
            log.error("No exercises found for lesson id: {} with specified filters", lessonId);
            throw new ResourceNotFoundException("Exercise", "lessonId", lessonId);
        }

        return ExerciseResponse.from(randomExercises.getContent().get(0), false);
    }

    /**
     * Get exercise by ID
     */
    @Transactional(readOnly = true)
    public ExerciseResponse getExerciseById(Long id) {
        log.debug("Fetching exercise with id: {}", id);
        Exercise exercise = findExerciseById(id);
        return ExerciseResponse.from(exercise, false);
    }

    /**
     * Get exercise details including the correct answer (admin usage).
     */
    @Transactional(readOnly = true)
    public ExerciseResponse getExerciseDetails(Long id) {
        log.debug("Fetching exercise details (with answer) for id: {}", id);
        Exercise exercise = findExerciseById(id);
        return ExerciseResponse.from(exercise, true);
    }

    /**
     * Paginated exercise list for admin UI.
     */
    @Transactional(readOnly = true)
    public Page<ExerciseResponse> getExercisePage(Long lessonId, Pageable pageable) {
        Page<Exercise> page = lessonId != null
                ? exerciseRepository.findByLessonId(lessonId, pageable)
                : exerciseRepository.findAll(pageable);
        return page.map(ex -> ExerciseResponse.from(ex, true));
    }

    /**
     * Get exercise entity by ID (internal use)
     */
    @Transactional(readOnly = true)
    public Exercise findExerciseById(Long id) {
        return exerciseRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Exercise not found with id: {}", id);
                    return new ResourceNotFoundException("Exercise", "id", id);
                });
    }

    /**
     * Create a new exercise
     */
    @Transactional
    public ExerciseResponse createExercise(CreateExerciseRequest request) {
        log.debug("Creating new exercise for lesson id: {}", request.getLessonId());

        // Verify lesson exists
        Lesson lesson = lessonService.findLessonById(request.getLessonId());

        // Validate and parse type
        Exercise.Type type;
        try {
            type = Exercise.Type.valueOf(request.getType().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("Invalid exercise type: {}", request.getType());
            throw new InvalidOperationException(
                    String.format("Invalid exercise type: %s. Valid values are: ENCODE, DECODE, AUDIO, SPEED_TEST, MULTI_CHOICE",
                            request.getType())
            );
        }

        // Validate and parse difficulty
        Exercise.Difficulty difficulty;
        try {
            difficulty = Exercise.Difficulty.valueOf(request.getDifficulty().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("Invalid difficulty value: {}", request.getDifficulty());
            throw new InvalidOperationException(
                    String.format("Invalid difficulty value: %s. Valid values are: EASY, MEDIUM, HARD",
                            request.getDifficulty())
            );
        }

        // Create new exercise
        Exercise exercise = Exercise.builder()
                .lesson(lesson)
                .type(type)
                .question(request.getQuestion())
                .correctAnswer(request.getCorrectAnswer())
                .options(request.getOptions())
                .difficulty(difficulty)
                .points(request.getPoints() != null ? request.getPoints() : 5)
                .timeLimit(request.getTimeLimit())
                .hint(request.getHint())
                .build();

        exercise = exerciseRepository.save(exercise);
        log.info("Exercise created successfully with id: {} for lesson: {}", exercise.getId(), lesson.getTitle());

        return ExerciseResponse.from(exercise, true);
    }

    /**
     * Update an existing exercise
     */
    @Transactional
    public ExerciseResponse updateExercise(Long id, UpdateExerciseRequest request) {
        log.debug("Updating exercise with id: {}", id);

        Exercise exercise = findExerciseById(id);

        // Update lesson if provided
        if (request.getLessonId() != null) {
            Lesson lesson = lessonService.findLessonById(request.getLessonId());
            exercise.setLesson(lesson);
        }

        // Update type if provided
        if (request.getType() != null && !request.getType().isEmpty()) {
            try {
                Exercise.Type type = Exercise.Type.valueOf(request.getType().toUpperCase());
                exercise.setType(type);
            } catch (IllegalArgumentException e) {
                log.error("Invalid exercise type: {}", request.getType());
                throw new InvalidOperationException(
                        String.format("Invalid exercise type: %s. Valid values are: ENCODE, DECODE, AUDIO, SPEED_TEST, MULTI_CHOICE",
                                request.getType())
                );
            }
        }

        // Update question if provided
        if (request.getQuestion() != null && !request.getQuestion().isEmpty()) {
            exercise.setQuestion(request.getQuestion());
        }

        // Update correct answer if provided
        if (request.getCorrectAnswer() != null && !request.getCorrectAnswer().isEmpty()) {
            exercise.setCorrectAnswer(request.getCorrectAnswer());
        }

        // Update options if provided
        if (request.getOptions() != null) {
            exercise.setOptions(request.getOptions());
        }

        // Update difficulty if provided
        if (request.getDifficulty() != null && !request.getDifficulty().isEmpty()) {
            try {
                Exercise.Difficulty difficulty = Exercise.Difficulty.valueOf(request.getDifficulty().toUpperCase());
                exercise.setDifficulty(difficulty);
            } catch (IllegalArgumentException e) {
                log.error("Invalid difficulty value: {}", request.getDifficulty());
                throw new InvalidOperationException(
                        String.format("Invalid difficulty value: %s. Valid values are: EASY, MEDIUM, HARD",
                                request.getDifficulty())
                );
            }
        }

        // Update points if provided
        if (request.getPoints() != null) {
            exercise.setPoints(request.getPoints());
        }

        // Update time limit if provided
        if (request.getTimeLimit() != null) {
            exercise.setTimeLimit(request.getTimeLimit());
        }

        // Update hint if provided
        if (request.getHint() != null) {
            exercise.setHint(request.getHint());
        }

        exercise = exerciseRepository.save(exercise);
        log.info("Exercise updated successfully with id: {}", id);

        return ExerciseResponse.from(exercise, true);
    }

    /**
     * Delete an exercise by ID
     */
    @Transactional
    public void deleteExercise(Long id) {
        log.debug("Deleting exercise with id: {}", id);

        Exercise exercise = findExerciseById(id);
        exerciseRepository.delete(exercise);
        log.info("Exercise deleted successfully with id: {}", id);
    }

    /**
     * Validate user answer against correct answer
     */
    public boolean validateAnswer(String userAnswer, String correctAnswer) {
        if (userAnswer == null || correctAnswer == null) {
            return false;
        }
        // Normalize both answers: trim and lowercase for comparison
        String normalizedUserAnswer = userAnswer.trim().toLowerCase();
        String normalizedCorrectAnswer = correctAnswer.trim().toLowerCase();
        return normalizedUserAnswer.equals(normalizedCorrectAnswer);
    }

    /**
     * Calculate accuracy percentage
     */
    public double calculateAccuracy(String userAnswer, String correctAnswer) {
        if (userAnswer == null || correctAnswer == null) {
            return 0.0;
        }

        String normalized1 = userAnswer.trim().toLowerCase();
        String normalized2 = correctAnswer.trim().toLowerCase();

        if (normalized1.equals(normalized2)) {
            return 100.0;
        }

        // Calculate Levenshtein distance for partial accuracy
        int distance = levenshteinDistance(normalized1, normalized2);
        int maxLength = Math.max(normalized1.length(), normalized2.length());

        if (maxLength == 0) {
            return 100.0;
        }

        double accuracy = (1.0 - (double) distance / maxLength) * 100.0;
        return Math.max(0.0, Math.min(100.0, accuracy)); // Clamp between 0 and 100
    }

    /**
     * Calculate Levenshtein distance between two strings
     */
    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[s1.length()][s2.length()];
    }

    /**
     * Get exercise count for a lesson
     */
    @Transactional(readOnly = true)
    public long getExerciseCountByLesson(Long lessonId) {
        log.debug("Counting exercises for lesson id: {}", lessonId);
        // Verify lesson exists
        lessonService.findLessonById(lessonId);
        return exerciseRepository.countByLessonId(lessonId);
    }

    /**
     * Search exercises by question text with pagination
     */
    @Transactional(readOnly = true)
    public Page<ExerciseResponse> searchExercisesByQuestion(String question, Pageable pageable) {
        log.debug("Searching exercises with question containing: {}", question);
        Page<Exercise> exercises = exerciseRepository.findByQuestionContainingIgnoreCase(question, pageable);
        return exercises.map(ex -> ExerciseResponse.from(ex, true));
    }
}
