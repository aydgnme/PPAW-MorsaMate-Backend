package me.aydgn.MorseMate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.dto.request.CreateCategoryRequest;
import me.aydgn.MorseMate.dto.request.UpdateCategoryRequest;
import me.aydgn.MorseMate.dto.response.CategoryResponse;
import me.aydgn.MorseMate.entity.Category;
import me.aydgn.MorseMate.exception.DuplicateResourceException;
import me.aydgn.MorseMate.exception.InvalidOperationException;
import me.aydgn.MorseMate.exception.ResourceNotFoundException;
import me.aydgn.MorseMate.repository.CategoryRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * Get all categories ordered by displayOrder
     */
    @Cacheable(value = "categories", key = "'all'")
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        log.debug("Fetching all categories ordered by displayOrder");
        List<Category> categories = categoryRepository.findAllByOrderByDisplayOrderAsc();
        return categories.stream()
                .map(category -> {
                    CategoryResponse response = CategoryResponse.from(category);
                    response.setLessonCount(categoryRepository.countLessons(category.getId()));
                    return response;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get all active categories ordered by displayOrder
     */
    @Cacheable(value = "categories", key = "'active'")
    @Transactional(readOnly = true)
    public List<CategoryResponse> getActiveCategories() {
        log.debug("Fetching all active categories ordered by displayOrder");
        List<Category> categories = categoryRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
        return categories.stream()
                .map(category -> {
                    CategoryResponse response = CategoryResponse.from(category);
                    response.setLessonCount(categoryRepository.countLessons(category.getId()));
                    return response;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get category by ID
     */
    @Cacheable(value = "categories", key = "#id")
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        log.debug("Fetching category with id: {}", id);
        Category category = findCategoryById(id);
        CategoryResponse response = CategoryResponse.from(category);
        response.setLessonCount(categoryRepository.countLessons(category.getId()));
        return response;
    }

    /**
     * Get category entity by ID (internal use)
     */
    @Cacheable(value = "categories", key = "'entity_' + #id")
    @Transactional(readOnly = true)
    public Category findCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Category not found with id: {}", id);
                    return new ResourceNotFoundException("Category", "id", id);
                });
    }

    /**
     * Create a new category
     */
    @CacheEvict(value = "categories", allEntries = true)
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        log.debug("Creating new category with name: {}", request.getName());

        // Check if category with same name already exists
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            log.error("Category already exists with name: {}", request.getName());
            throw new DuplicateResourceException("Category", "name", request.getName());
        }

        // Create new category
        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .displayOrder(request.getDisplayOrder())
                .iconUrl(request.getIconUrl())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        category = categoryRepository.save(category);
        log.info("Category created successfully with id: {} and name: {}", category.getId(), category.getName());

        return CategoryResponse.from(category);
    }

    /**
     * Update an existing category
     */
    @CacheEvict(value = "categories", allEntries = true)
    @Transactional
    public CategoryResponse updateCategory(Long id, UpdateCategoryRequest request) {
        log.debug("Updating category with id: {}", id);

        Category category = findCategoryById(id);

        // Check if new name conflicts with existing category
        if (request.getName() != null && !request.getName().isEmpty()) {
            if (!request.getName().equalsIgnoreCase(category.getName()) &&
                categoryRepository.existsByNameIgnoreCase(request.getName())) {
                log.error("Category already exists with name: {}", request.getName());
                throw new DuplicateResourceException("Category", "name", request.getName());
            }
            category.setName(request.getName());
        }

        // Update other fields if provided
        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }

        if (request.getDisplayOrder() != null) {
            category.setDisplayOrder(request.getDisplayOrder());
        }

        if (request.getIconUrl() != null) {
            category.setIconUrl(request.getIconUrl());
        }

        if (request.getIsActive() != null) {
            category.setIsActive(request.getIsActive());
        }

        category = categoryRepository.save(category);
        log.info("Category updated successfully with id: {}", id);

        return CategoryResponse.from(category);
    }

    /**
     * Delete a category by ID
     */
    @CacheEvict(value = "categories", allEntries = true)
    @Transactional
    public void deleteCategory(Long id) {
        log.debug("Deleting category with id: {}", id);

        Category category = findCategoryById(id);

        // Check if category has associated lessons
        long lessonCount = categoryRepository.countLessons(id);
        if (lessonCount > 0) {
            log.error("Cannot delete category with id: {} as it has {} associated lessons", id, lessonCount);
            throw new InvalidOperationException(
                    String.format("Cannot delete category '%s' as it has %d associated lessons. " +
                            "Please delete or reassign the lessons first.", category.getName(), lessonCount)
            );
        }

        categoryRepository.delete(category);
        log.info("Category deleted successfully with id: {}", id);
    }

    /**
     * Get category with lessons
     */
    @Cacheable(value = "categories", key = "'with_lessons_' + #id")
    @Transactional(readOnly = true)
    public Category getCategoryWithLessons(Long id) {
        log.debug("Fetching category with lessons for id: {}", id);
        return categoryRepository.findWithLessonsById(id)
                .orElseThrow(() -> {
                    log.error("Category not found with id: {}", id);
                    return new ResourceNotFoundException("Category", "id", id);
                });
    }

    /**
     * Get total lesson count for a category
     */
    @Cacheable(value = "categories", key = "'lesson_count_' + #categoryId")
    @Transactional(readOnly = true)
    public long getLessonCount(Long categoryId) {
        log.debug("Counting lessons for category id: {}", categoryId);
        // Verify category exists
        findCategoryById(categoryId);
        return categoryRepository.countLessons(categoryId);
    }
}
