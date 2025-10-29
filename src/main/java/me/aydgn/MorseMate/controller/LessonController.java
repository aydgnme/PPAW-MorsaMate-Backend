package me.aydgn.MorseMate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.dto.request.CreateLessonRequest;
import me.aydgn.MorseMate.dto.request.UpdateLessonRequest;
import me.aydgn.MorseMate.dto.response.ApiMessage;
import me.aydgn.MorseMate.dto.response.LessonResponse;
import me.aydgn.MorseMate.service.LessonService;
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
 * REST Controller for Lesson management
 * Provides endpoints for CRUD operations on lessons
 */
@RestController
@RequestMapping("/${api.version}/lessons")
@RequiredArgsConstructor
@Slf4j
public class LessonController {

    private final LessonService lessonService;

    /**
     * GET /v1/lessons/{id}
     * Get a single lesson by ID
     * Public endpoint - no authentication required
     */
    @GetMapping("/{id}")
    public ResponseEntity<LessonResponse> getLessonById(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean includeExercises) {
        log.debug("GET /v1/lessons/{} - Fetching lesson (includeExercises: {})", id, includeExercises);
        LessonResponse lesson = lessonService.getLessonById(id, includeExercises);
        return ResponseEntity.ok(lesson);
    }

    /**
     * GET /v1/lessons?categoryId={categoryId}
     * Get all lessons for a specific category
     * Public endpoint - no authentication required
     */
    @GetMapping
    public ResponseEntity<List<LessonResponse>> getLessonsByCategoryId(
            @RequestParam Long categoryId) {
        log.debug("GET /v1/lessons?categoryId={} - Fetching lessons for category", categoryId);
        List<LessonResponse> lessons = lessonService.getLessonsByCategoryId(categoryId);
        return ResponseEntity.ok(lessons);
    }

    /**
     * GET /v1/lessons/search?title={title}
     * Search lessons by title with pagination
     * Public endpoint - no authentication required
     */
    @GetMapping("/search")
    public ResponseEntity<Page<LessonResponse>> searchLessonsByTitle(
            @RequestParam String title,
            @PageableDefault(size = 20, sort = "title", direction = Sort.Direction.ASC) Pageable pageable) {
        log.debug("GET /v1/lessons/search?title={} - Searching lessons", title);
        Page<LessonResponse> lessons = lessonService.searchLessonsByTitle(title, pageable);
        return ResponseEntity.ok(lessons);
    }

    /**
     * POST /v1/lessons
     * Create a new lesson
     * Admin only endpoint
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LessonResponse> createLesson(@Valid @RequestBody CreateLessonRequest request) {
        log.info("POST /v1/lessons - Creating new lesson: {}", request.getTitle());
        LessonResponse created = lessonService.createLesson(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /v1/lessons/{id}
     * Update an existing lesson
     * Admin only endpoint
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LessonResponse> updateLesson(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLessonRequest request) {
        log.info("PUT /v1/lessons/{} - Updating lesson", id);
        LessonResponse updated = lessonService.updateLesson(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /v1/lessons/{id}
     * Delete a lesson
     * Admin only endpoint
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiMessage> deleteLesson(@PathVariable Long id) {
        log.info("DELETE /v1/lessons/{} - Deleting lesson", id);
        lessonService.deleteLesson(id);
        return ResponseEntity.ok(new ApiMessage("Lesson deleted successfully"));
    }

    /**
     * GET /v1/lessons/{id}/exercise-count
     * Get total exercise count for a lesson
     * Public endpoint - no authentication required
     */
    @GetMapping("/{id}/exercise-count")
    public ResponseEntity<Long> getExerciseCount(@PathVariable Long id) {
        log.debug("GET /v1/lessons/{}/exercise-count - Counting exercises", id);
        long count = lessonService.getExerciseCount(id);
        return ResponseEntity.ok(count);
    }

    /**
     * PATCH /v1/lessons/{id}/order
     * Update lesson order index
     * Admin only endpoint
     */
    @PatchMapping("/{id}/order")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiMessage> updateLessonOrder(
            @PathVariable Long id,
            @RequestParam int orderIndex) {
        log.info("PATCH /v1/lessons/{}/order - Updating order to {}", id, orderIndex);
        lessonService.updateLessonOrder(id, orderIndex);
        return ResponseEntity.ok(new ApiMessage("Lesson order updated successfully"));
    }
}
