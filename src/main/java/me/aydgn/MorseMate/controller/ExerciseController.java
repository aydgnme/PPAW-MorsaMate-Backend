package me.aydgn.MorseMate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.dto.request.CreateExerciseRequest;
import me.aydgn.MorseMate.dto.request.UpdateExerciseRequest;
import me.aydgn.MorseMate.dto.response.ApiMessage;
import me.aydgn.MorseMate.dto.response.ExerciseResponse;
import me.aydgn.MorseMate.service.ExerciseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Exercise management
 * Provides endpoints for CRUD operations on exercises
 */
@RestController
@RequestMapping("/${api.version}/exercises")
@RequiredArgsConstructor
@Slf4j
public class ExerciseController {

    private final ExerciseService exerciseService;

    /**
     * GET /v1/exercises/{id}
     * Get a single exercise by ID
     * Authenticated users only
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ExerciseResponse> getExerciseById(@PathVariable Long id) {
        log.debug("GET /v1/exercises/{} - Fetching exercise", id);
        ExerciseResponse exercise = exerciseService.getExerciseById(id);
        return ResponseEntity.ok(exercise);
    }

    /**
     * GET /v1/exercises?lessonId={lessonId}
     * Get all exercises for a specific lesson
     * Authenticated users only
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ExerciseResponse>> getExercisesByLessonId(
            @RequestParam Long lessonId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String difficulty) {
        log.debug("GET /v1/exercises?lessonId={} (type: {}, difficulty: {})", lessonId, type, difficulty);

        List<ExerciseResponse> exercises;

        // Apply filters based on provided parameters
        boolean hasType = type != null && !type.isEmpty();
        boolean hasDifficulty = difficulty != null && !difficulty.isEmpty();

        if (hasType && hasDifficulty) {
            // Both filters provided - get all and filter in service layer
            // Note: This requires a new service method for combined filtering
            exercises = exerciseService.getExercisesByLessonAndType(lessonId, type);
            // For now, just use type filter - TODO: add combined filter method
            log.warn("Both type and difficulty provided - only type filter applied. Consider adding combined filter.");
        } else if (hasType) {
            exercises = exerciseService.getExercisesByLessonAndType(lessonId, type);
        } else if (hasDifficulty) {
            exercises = exerciseService.getExercisesByLessonAndDifficulty(lessonId, difficulty);
        } else {
            exercises = exerciseService.getExercisesByLessonId(lessonId);
        }

        return ResponseEntity.ok(exercises);
    }

    /**
     * GET /v1/exercises/random?lessonId={lessonId}&type={type}&difficulty={difficulty}
     * Get a random exercise with optional filters
     * Authenticated users only
     */
    @GetMapping("/random")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ExerciseResponse> getRandomExercise(
            @RequestParam Long lessonId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String difficulty) {
        log.debug("GET /v1/exercises/random?lessonId={} (type: {}, difficulty: {})", lessonId, type, difficulty);
        ExerciseResponse exercise = exerciseService.getRandomExercise(lessonId, type, difficulty);
        return ResponseEntity.ok(exercise);
    }

    /**
     * GET /v1/exercises/search?question={question}
     * Search exercises by question text with pagination
     * Admin only endpoint
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ExerciseResponse>> searchExercisesByQuestion(
            @RequestParam String question,
            @PageableDefault(size = 20, sort = "question", direction = Sort.Direction.ASC) Pageable pageable) {
        log.debug("GET /v1/exercises/search?question={}", question);
        Page<ExerciseResponse> exercises = exerciseService.searchExercisesByQuestion(question, pageable);
        return ResponseEntity.ok(exercises);
    }

    /**
     * POST /v1/exercises
     * Create a new exercise
     * Admin only endpoint
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ExerciseResponse> createExercise(@Valid @RequestBody CreateExerciseRequest request) {
        log.info("POST /v1/exercises - Creating new exercise for lesson: {}", request.getLessonId());
        ExerciseResponse created = exerciseService.createExercise(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /v1/exercises/{id}
     * Update an existing exercise
     * Admin only endpoint
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ExerciseResponse> updateExercise(
            @PathVariable Long id,
            @Valid @RequestBody UpdateExerciseRequest request) {
        log.info("PUT /v1/exercises/{} - Updating exercise", id);
        ExerciseResponse updated = exerciseService.updateExercise(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /v1/exercises/{id}
     * Delete an exercise
     * Admin only endpoint
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiMessage> deleteExercise(@PathVariable Long id) {
        log.info("DELETE /v1/exercises/{} - Deleting exercise", id);
        exerciseService.deleteExercise(id);
        return ResponseEntity.ok(new ApiMessage("Exercise deleted successfully"));
    }

    /**
     * GET /v1/exercises/count?lessonId={lessonId}
     * Get exercise count for a lesson
     * Public endpoint
     */
    @GetMapping("/count")
    public ResponseEntity<Long> getExerciseCount(@RequestParam Long lessonId) {
        log.debug("GET /v1/exercises/count?lessonId={}", lessonId);
        long count = exerciseService.getExerciseCountByLesson(lessonId);
        return ResponseEntity.ok(count);
    }
}
