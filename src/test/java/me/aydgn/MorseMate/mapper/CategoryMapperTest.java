package me.aydgn.MorseMate.mapper;

import me.aydgn.MorseMate.dto.request.CreateCategoryRequest;
import me.aydgn.MorseMate.dto.request.UpdateCategoryRequest;
import me.aydgn.MorseMate.dto.response.CategoryResponse;
import me.aydgn.MorseMate.entity.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CategoryMapper Tests")
class CategoryMapperTest {

    private CategoryMapper categoryMapper;
    private Category testCategory;
    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        categoryMapper = new CategoryMapper();
        testTime = LocalDateTime.now();

        testCategory = Category.builder()
                .id(1L)
                .name("Test Category")
                .description("Test Description")
                .displayOrder(1)
                .iconUrl("https://example.com/icon.png")
                .isActive(true)
                .build();
        testCategory.setCreatedAt(testTime);
        testCategory.setUpdatedAt(testTime);
    }

    @Test
    @DisplayName("Should convert Category entity to CategoryResponse")
    void toResponse_Success() {
        // When
        CategoryResponse response = categoryMapper.toResponse(testCategory);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Test Category");
        assertThat(response.getDescription()).isEqualTo("Test Description");
        assertThat(response.getDisplayOrder()).isEqualTo(1);
        assertThat(response.getIconUrl()).isEqualTo("https://example.com/icon.png");
        assertThat(response.getIsActive()).isTrue();
        assertThat(response.getCreatedAt()).isEqualTo(testTime);
    }

    @Test
    @DisplayName("Should return null when converting null entity to response")
    void toResponse_NullEntity() {
        // When
        CategoryResponse response = categoryMapper.toResponse(null);

        // Then
        assertThat(response).isNull();
    }

    @Test
    @DisplayName("Should handle Category with null fields")
    void toResponse_NullFields() {
        // Given
        Category category = Category.builder()
                .id(2L)
                .name("Minimal Category")
                .isActive(true)
                .build();

        // When
        CategoryResponse response = categoryMapper.toResponse(category);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getName()).isEqualTo("Minimal Category");
        assertThat(response.getDescription()).isNull();
        assertThat(response.getDisplayOrder()).isNull();
        assertThat(response.getIconUrl()).isNull();
        assertThat(response.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("Should convert list of entities to list of responses")
    void toResponseList_Success() {
        // Given
        Category category2 = Category.builder()
                .id(2L)
                .name("Second Category")
                .description("Second Description")
                .displayOrder(2)
                .iconUrl("https://example.com/icon2.png")
                .isActive(false)
                .build();

        List<Category> categories = Arrays.asList(testCategory, category2);

        // When
        List<CategoryResponse> responses = categoryMapper.toResponseList(categories);

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getName()).isEqualTo("Test Category");
        assertThat(responses.get(0).getIsActive()).isTrue();
        assertThat(responses.get(1).getName()).isEqualTo("Second Category");
        assertThat(responses.get(1).getIsActive()).isFalse();
    }

    @Test
    @DisplayName("Should return null when converting null list")
    void toResponseList_NullList() {
        // When
        List<CategoryResponse> responses = categoryMapper.toResponseList(null);

        // Then
        assertThat(responses).isNull();
    }

    @Test
    @DisplayName("Should return empty list when converting empty list")
    void toResponseList_EmptyList() {
        // When
        List<CategoryResponse> responses = categoryMapper.toResponseList(Arrays.asList());

        // Then
        assertThat(responses).isEmpty();
    }

    @Test
    @DisplayName("Should convert CreateCategoryRequest to Category entity")
    void toEntity_FromCreateRequest() {
        // Given
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("New Category")
                .description("New Description")
                .displayOrder(5)
                .iconUrl("https://example.com/new-icon.png")
                .isActive(true)
                .build();

        // When
        Category category = categoryMapper.toEntity(request);

        // Then
        assertThat(category).isNotNull();
        assertThat(category.getId()).isNull(); // Not set by mapper
        assertThat(category.getName()).isEqualTo("New Category");
        assertThat(category.getDescription()).isEqualTo("New Description");
        assertThat(category.getDisplayOrder()).isEqualTo(5);
        assertThat(category.getIconUrl()).isEqualTo("https://example.com/new-icon.png");
        assertThat(category.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("Should return null when converting null CreateRequest")
    void toEntity_NullCreateRequest() {
        // When
        Category category = categoryMapper.toEntity(null);

        // Then
        assertThat(category).isNull();
    }

    @Test
    @DisplayName("Should set isActive to true when null in CreateRequest")
    void toEntity_DefaultIsActive() {
        // Given
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("Category with default active")
                .isActive(null)
                .build();

        // When
        Category category = categoryMapper.toEntity(request);

        // Then
        assertThat(category).isNotNull();
        assertThat(category.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("Should handle CreateRequest with minimal fields")
    void toEntity_MinimalRequest() {
        // Given
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("Minimal Category")
                .build();

        // When
        Category category = categoryMapper.toEntity(request);

        // Then
        assertThat(category).isNotNull();
        assertThat(category.getName()).isEqualTo("Minimal Category");
        assertThat(category.getDescription()).isNull();
        assertThat(category.getDisplayOrder()).isNull();
        assertThat(category.getIconUrl()).isNull();
        assertThat(category.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("Should update entity with all fields from UpdateRequest")
    void updateEntity_AllFields() {
        // Given
        UpdateCategoryRequest request = UpdateCategoryRequest.builder()
                .name("Updated Name")
                .description("Updated Description")
                .displayOrder(10)
                .iconUrl("https://example.com/updated-icon.png")
                .isActive(false)
                .build();

        // When
        categoryMapper.updateEntity(testCategory, request);

        // Then
        assertThat(testCategory.getName()).isEqualTo("Updated Name");
        assertThat(testCategory.getDescription()).isEqualTo("Updated Description");
        assertThat(testCategory.getDisplayOrder()).isEqualTo(10);
        assertThat(testCategory.getIconUrl()).isEqualTo("https://example.com/updated-icon.png");
        assertThat(testCategory.getIsActive()).isFalse();
        assertThat(testCategory.getId()).isEqualTo(1L); // ID should not change
    }

    @Test
    @DisplayName("Should update only provided fields from UpdateRequest")
    void updateEntity_PartialUpdate() {
        // Given
        UpdateCategoryRequest request = UpdateCategoryRequest.builder()
                .description("Only description updated")
                .build();

        String originalName = testCategory.getName();
        Integer originalOrder = testCategory.getDisplayOrder();
        String originalIcon = testCategory.getIconUrl();
        Boolean originalActive = testCategory.getIsActive();

        // When
        categoryMapper.updateEntity(testCategory, request);

        // Then
        assertThat(testCategory.getName()).isEqualTo(originalName);
        assertThat(testCategory.getDescription()).isEqualTo("Only description updated");
        assertThat(testCategory.getDisplayOrder()).isEqualTo(originalOrder);
        assertThat(testCategory.getIconUrl()).isEqualTo(originalIcon);
        assertThat(testCategory.getIsActive()).isEqualTo(originalActive);
    }

    @Test
    @DisplayName("Should not update when UpdateRequest is null")
    void updateEntity_NullRequest() {
        // Given
        String originalName = testCategory.getName();

        // When
        categoryMapper.updateEntity(testCategory, null);

        // Then
        assertThat(testCategory.getName()).isEqualTo(originalName);
    }

    @Test
    @DisplayName("Should not update when entity is null")
    void updateEntity_NullEntity() {
        // Given
        UpdateCategoryRequest request = UpdateCategoryRequest.builder()
                .name("Should not fail")
                .build();

        // When & Then - should not throw exception
        categoryMapper.updateEntity(null, request);
    }

    @Test
    @DisplayName("Should not update name when empty string in UpdateRequest")
    void updateEntity_EmptyName() {
        // Given
        UpdateCategoryRequest request = UpdateCategoryRequest.builder()
                .name("")
                .build();

        String originalName = testCategory.getName();

        // When
        categoryMapper.updateEntity(testCategory, request);

        // Then
        assertThat(testCategory.getName()).isEqualTo(originalName);
    }

    @Test
    @DisplayName("Should allow setting isActive to false")
    void updateEntity_IsActiveFalse() {
        // Given
        testCategory.setIsActive(true);
        UpdateCategoryRequest request = UpdateCategoryRequest.builder()
                .isActive(false)
                .build();

        // When
        categoryMapper.updateEntity(testCategory, request);

        // Then
        assertThat(testCategory.getIsActive()).isFalse();
    }

    @Test
    @DisplayName("Should copy entity successfully")
    void copyEntity_Success() {
        // When
        Category copy = categoryMapper.copyEntity(testCategory);

        // Then
        assertThat(copy).isNotNull();
        assertThat(copy).isNotSameAs(testCategory);
        assertThat(copy.getName()).isEqualTo(testCategory.getName());
        assertThat(copy.getDescription()).isEqualTo(testCategory.getDescription());
        assertThat(copy.getDisplayOrder()).isEqualTo(testCategory.getDisplayOrder());
        assertThat(copy.getIconUrl()).isEqualTo(testCategory.getIconUrl());
        assertThat(copy.getIsActive()).isEqualTo(testCategory.getIsActive());
        assertThat(copy.getCreatedAt()).isEqualTo(testCategory.getCreatedAt());
        assertThat(copy.getUpdatedAt()).isEqualTo(testCategory.getUpdatedAt());
        assertThat(copy.getId()).isNull(); // ID is not copied in builder
    }

    @Test
    @DisplayName("Should return null when copying null entity")
    void copyEntity_NullEntity() {
        // When
        Category copy = categoryMapper.copyEntity(null);

        // Then
        assertThat(copy).isNull();
    }

    @Test
    @DisplayName("Should handle bidirectional conversion")
    void bidirectionalConversion() {
        // Given - Create request
        CreateCategoryRequest createRequest = CreateCategoryRequest.builder()
                .name("Bidirectional Test")
                .description("Testing conversion")
                .displayOrder(3)
                .iconUrl("https://example.com/test.png")
                .isActive(true)
                .build();

        // When - Convert to entity
        Category entity = categoryMapper.toEntity(createRequest);
        entity.setId(100L);
        entity.setCreatedAt(LocalDateTime.now());

        // Then - Convert back to response
        CategoryResponse response = categoryMapper.toResponse(entity);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getName()).isEqualTo(createRequest.getName());
        assertThat(response.getDescription()).isEqualTo(createRequest.getDescription());
        assertThat(response.getDisplayOrder()).isEqualTo(createRequest.getDisplayOrder());
        assertThat(response.getIconUrl()).isEqualTo(createRequest.getIconUrl());
        assertThat(response.getIsActive()).isEqualTo(createRequest.getIsActive());
        assertThat(response.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should preserve data integrity through update cycle")
    void updateCycleIntegrity() {
        // Given
        Category original = Category.builder()
                .id(50L)
                .name("Original")
                .description("Original Description")
                .displayOrder(5)
                .iconUrl("https://example.com/original.png")
                .isActive(true)
                .build();

        UpdateCategoryRequest update1 = UpdateCategoryRequest.builder()
                .name("Updated Once")
                .displayOrder(10)
                .build();

        UpdateCategoryRequest update2 = UpdateCategoryRequest.builder()
                .description("Updated Description")
                .isActive(false)
                .build();

        // When - Apply multiple updates
        categoryMapper.updateEntity(original, update1);
        categoryMapper.updateEntity(original, update2);

        // Then
        assertThat(original.getName()).isEqualTo("Updated Once");
        assertThat(original.getDescription()).isEqualTo("Updated Description");
        assertThat(original.getDisplayOrder()).isEqualTo(10);
        assertThat(original.getIconUrl()).isEqualTo("https://example.com/original.png");
        assertThat(original.getIsActive()).isFalse();
        assertThat(original.getId()).isEqualTo(50L);
    }
}
