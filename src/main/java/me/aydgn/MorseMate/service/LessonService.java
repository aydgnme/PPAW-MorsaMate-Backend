package me.aydgn.MorseMate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.dto.request.CreateLessonRequest;
import me.aydgn.MorseMate.dto.request.UpdateLessonRequest;
import me.aydgn.MorseMate.dto.response.LessonResponse;
import me.aydgn.MorseMate.entity.Category;
import me.aydgn.MorseMate.entity.Lesson;
import me.aydgn.MorseMate.exception.InvalidOperationException;
import me.aydgn.MorseMate.exception.ResourceNotFoundException;
import me.aydgn.MorseMate.repository.LessonRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LessonService {

    private final LessonRepository lessonRepository;
    private final CategoryService categoryService;

    /**
     * Get all lessons for a specific category ordered by orderIndex
     */
    @Cacheable(value = "lessons", key = "'category_' + #categoryId")
    @Transactional(readOnly = true)
    public List<LessonResponse> getLessonsByCategoryId(Long categoryId) {
        log.debug("Fetching lessons for category id: {}", categoryId);
        // Verify category exists
        categoryService.findCategoryById(categoryId);

        List<Lesson> lessons = lessonRepository.findByCategoryIdOrderByOrderIndexAsc(categoryId);
        return lessons.stream()
                .map(lesson -> LessonResponse.from(lesson, false))
                .collect(Collectors.toList());
    }

    /**
     * Get lesson by ID
     */
    @Cacheable(value = "lessons", key = "#id + '_' + #includeExercises")
    @Transactional(readOnly = true)
    public LessonResponse getLessonById(Long id, boolean includeExercises) {
        log.debug("Fetching lesson with id: {} (includeExercises: {})", id, includeExercises);

        Lesson lesson;
        if (includeExercises) {
            lesson = lessonRepository.findWithExercisesById(id)
                    .orElseThrow(() -> {
                        log.error("Lesson not found with id: {}", id);
                        return new ResourceNotFoundException("Lesson", "id", id);
                    });
        } else {
            lesson = findLessonById(id);
        }

        return LessonResponse.from(lesson, includeExercises);
    }

    /**
     * Get lesson entity by ID (internal use)
     */
    @Cacheable(value = "lessons", key = "'entity_' + #id")
    @Transactional(readOnly = true)
    public Lesson findLessonById(Long id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Lesson not found with id: {}", id);
                    return new ResourceNotFoundException("Lesson", "id", id);
                });
    }

    /**
     * Search lessons by title
     */
    @Transactional(readOnly = true)
    public Page<LessonResponse> searchLessonsByTitle(String title, Pageable pageable) {
        log.debug("Searching lessons with title containing: {}", title);
        Page<Lesson> lessons = lessonRepository.findByTitleContainingIgnoreCase(title, pageable);
        return lessons.map(lesson -> LessonResponse.from(lesson, false));
    }

    /**
     * Create a new lesson
     */
    @CacheEvict(value = "lessons", allEntries = true)
    @Transactional
    public LessonResponse createLesson(CreateLessonRequest request) {
        log.debug("Creating new lesson with title: {}", request.getTitle());

        // Verify category exists
        Category category = categoryService.findCategoryById(request.getCategoryId());

        // Validate and parse difficulty
        Lesson.Difficulty difficulty;
        try {
            difficulty = Lesson.Difficulty.valueOf(request.getDifficulty().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("Invalid difficulty value: {}", request.getDifficulty());
            throw new InvalidOperationException(
                    String.format("Invalid difficulty value: %s. Valid values are: BEGINNER, INTERMEDIATE, ADVANCED",
                            request.getDifficulty())
            );
        }

        // Create new lesson
        Lesson lesson = Lesson.builder()
                .category(category)
                .title(request.getTitle())
                .description(request.getDescription())
                .difficulty(difficulty)
                .content(request.getContent())
                .orderIndex(request.getOrderIndex())
                .pointsReward(request.getPointsReward() != null ? request.getPointsReward() : 10)
                .estimatedDuration(request.getEstimatedDuration())
                .build();

        lesson = lessonRepository.save(lesson);
        log.info("Lesson created successfully with id: {} and title: {}", lesson.getId(), lesson.getTitle());

        return LessonResponse.from(lesson, false);
    }

    /**
     * Update an existing lesson
     */
    @CacheEvict(value = "lessons", allEntries = true)
    @Transactional
    public LessonResponse updateLesson(Long id, UpdateLessonRequest request) {
        log.debug("Updating lesson with id: {}", id);

        Lesson lesson = findLessonById(id);

        // Update category if provided
        if (request.getCategoryId() != null) {
            Category category = categoryService.findCategoryById(request.getCategoryId());
            lesson.setCategory(category);
        }

        // Update title if provided
        if (request.getTitle() != null && !request.getTitle().isEmpty()) {
            lesson.setTitle(request.getTitle());
        }

        // Update description if provided
        if (request.getDescription() != null) {
            lesson.setDescription(request.getDescription());
        }

        // Update difficulty if provided
        if (request.getDifficulty() != null && !request.getDifficulty().isEmpty()) {
            try {
                Lesson.Difficulty difficulty = Lesson.Difficulty.valueOf(request.getDifficulty().toUpperCase());
                lesson.setDifficulty(difficulty);
            } catch (IllegalArgumentException e) {
                log.error("Invalid difficulty value: {}", request.getDifficulty());
                throw new InvalidOperationException(
                        String.format("Invalid difficulty value: %s. Valid values are: BEGINNER, INTERMEDIATE, ADVANCED",
                                request.getDifficulty())
                );
            }
        }

        // Update content if provided
        if (request.getContent() != null) {
            lesson.setContent(request.getContent());
        }

        // Update orderIndex if provided
        if (request.getOrderIndex() != null) {
            lesson.setOrderIndex(request.getOrderIndex());
        }

        // Update pointsReward if provided
        if (request.getPointsReward() != null) {
            lesson.setPointsReward(request.getPointsReward());
        }

        // Update estimatedDuration if provided
        if (request.getEstimatedDuration() != null) {
            lesson.setEstimatedDuration(request.getEstimatedDuration());
        }

        lesson = lessonRepository.save(lesson);
        log.info("Lesson updated successfully with id: {}", id);

        return LessonResponse.from(lesson, false);
    }

    /**
     * Delete a lesson by ID
     */
    @CacheEvict(value = "lessons", allEntries = true)
    @Transactional
    public void deleteLesson(Long id) {
        log.debug("Deleting lesson with id: {}", id);

        Lesson lesson = findLessonById(id);

        // Check if lesson has associated exercises
        long exerciseCount = lessonRepository.countExercises(id);
        if (exerciseCount > 0) {
            log.error("Cannot delete lesson with id: {} as it has {} associated exercises", id, exerciseCount);
            throw new InvalidOperationException(
                    String.format("Cannot delete lesson '%s' as it has %d associated exercises. " +
                            "Please delete the exercises first.", lesson.getTitle(), exerciseCount)
            );
        }

        lessonRepository.delete(lesson);
        log.info("Lesson deleted successfully with id: {}", id);
    }

    /**
     * Get total exercise count for a lesson
     */
    @Cacheable(value = "lessons", key = "'exercise_count_' + #lessonId")
    @Transactional(readOnly = true)
    public long getExerciseCount(Long lessonId) {
        log.debug("Counting exercises for lesson id: {}", lessonId);
        // Verify lesson exists
        findLessonById(lessonId);
        return lessonRepository.countExercises(lessonId);
    }

    /**
     * Update lesson order index
     */
    @CacheEvict(value = "lessons", allEntries = true)
    @Transactional
    public void updateLessonOrder(Long lessonId, int orderIndex) {
        log.debug("Updating order index for lesson id: {} to {}", lessonId, orderIndex);
        // Verify lesson exists
        findLessonById(lessonId);

        int updated = lessonRepository.setOrderIndex(lessonId, orderIndex);
        if (updated == 0) {
            log.warn("No rows updated when setting order index for lesson id: {}", lessonId);
        } else {
            log.info("Order index updated successfully for lesson id: {}", lessonId);
        }
    }
}