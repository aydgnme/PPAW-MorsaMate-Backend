package me.aydgn.MorseMate.mapper;

import me.aydgn.MorseMate.dto.request.CreateCategoryRequest;
import me.aydgn.MorseMate.dto.request.UpdateCategoryRequest;
import me.aydgn.MorseMate.dto.response.CategoryResponse;
import me.aydgn.MorseMate.entity.Category;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Category entity and DTOs
 */
@Component
public class CategoryMapper {

    /**
     * Convert Category entity to CategoryResponse DTO
     */
    public CategoryResponse toResponse(Category category) {
        if (category == null) {
            return null;
        }

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .displayOrder(category.getDisplayOrder())
                .iconUrl(category.getIconUrl())
                .isActive(category.getIsActive())
                .createdAt(category.getCreatedAt())
                .build();
    }

    /**
     * Convert list of Category entities to list of CategoryResponse DTOs
     */
    public List<CategoryResponse> toResponseList(List<Category> categories) {
        if (categories == null) {
            return null;
        }

        return categories.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert CreateCategoryRequest DTO to Category entity
     */
    public Category toEntity(CreateCategoryRequest request) {
        if (request == null) {
            return null;
        }

        return Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .displayOrder(request.getDisplayOrder())
                .iconUrl(request.getIconUrl())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
    }

    /**
     * Update existing Category entity with data from UpdateCategoryRequest
     * Only updates non-null fields from the request
     */
    public void updateEntity(Category category, UpdateCategoryRequest request) {
        if (category == null || request == null) {
            return;
        }

        if (request.getName() != null && !request.getName().isEmpty()) {
            category.setName(request.getName());
        }

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
    }

    /**
     * Create a partial copy of Category entity (useful for testing)
     */
    public Category copyEntity(Category source) {
        if (source == null) {
            return null;
        }

        Category copy = Category.builder()
                .name(source.getName())
                .description(source.getDescription())
                .displayOrder(source.getDisplayOrder())
                .iconUrl(source.getIconUrl())
                .isActive(source.getIsActive())
                .build();

        copy.setCreatedAt(source.getCreatedAt());
        copy.setUpdatedAt(source.getUpdatedAt());

        return copy;
    }
}
