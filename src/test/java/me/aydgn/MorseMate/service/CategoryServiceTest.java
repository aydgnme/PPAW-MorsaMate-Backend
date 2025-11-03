package me.aydgn.MorseMate.service;

import me.aydgn.MorseMate.dto.request.CreateCategoryRequest;
import me.aydgn.MorseMate.dto.request.UpdateCategoryRequest;
import me.aydgn.MorseMate.dto.response.CategoryResponse;
import me.aydgn.MorseMate.entity.Category;
import me.aydgn.MorseMate.exception.DuplicateResourceException;
import me.aydgn.MorseMate.exception.InvalidOperationException;
import me.aydgn.MorseMate.exception.ResourceNotFoundException;
import me.aydgn.MorseMate.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService Tests")
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category1;
    private Category category2;

    @BeforeEach
    void setUp() {
        category1 = Category.builder()
                .id(1L)
                .name("Basics")
                .description("Basic Morse code lessons")
                .iconUrl("data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 64 64'><rect width='64' height='64' rx='12' fill='%230ea5e9'/><circle cx='18' cy='32' r='6' fill='white'/><rect x='30' y='26' width='24' height='12' rx='6' fill='white'/></svg>")
                .displayOrder(1)
                .isActive(true)
                .build();
        category1.setCreatedAt(LocalDateTime.now());

        category2 = Category.builder()
                .id(2L)
                .name("Advanced")
                .description("Advanced Morse code lessons")
                .iconUrl("data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 64 64'><rect width='64' height='64' rx='12' fill='%237a3aed'/><rect x='10' y='26' width='20' height='12' rx='6' fill='white'/><circle cx='36' cy='32' r='6' fill='white'/><rect x='42' y='26' width='12' height='12' rx='6' fill='white'/></svg>")
                .displayOrder(2)
                .isActive(true)
                .build();
        category2.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should get all categories ordered by displayOrder")
    void getAllCategories_Success() {
        // Given
        List<Category> categories = Arrays.asList(category1, category2);
        when(categoryRepository.findAllByOrderByDisplayOrderAsc()).thenReturn(categories);

        // When
        List<CategoryResponse> result = categoryService.getAllCategories();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Basics");
        assertThat(result.get(1).getName()).isEqualTo("Advanced");
        verify(categoryRepository, times(1)).findAllByOrderByDisplayOrderAsc();
    }

    @Test
    @DisplayName("Should get category by ID successfully")
    void getCategoryById_Success() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));

        // When
        CategoryResponse result = categoryService.getCategoryById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Basics");
        assertThat(result.getDescription()).isEqualTo("Basic Morse code lessons");
        verify(categoryRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when category not found")
    void getCategoryById_NotFound() {
        // Given
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryService.getCategoryById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found with id: '999'");
        verify(categoryRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should create category successfully")
    void createCategory_Success() {
        // Given
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("Intermediate")
                .description("Intermediate level lessons")
                .iconUrl("https://example.com/icon-intermediate.png")
                .displayOrder(3)
                .isActive(true)
                .build();

        Category savedCategory = Category.builder()
                .id(3L)
                .name("Intermediate")
                .description("Intermediate level lessons")
                .iconUrl("https://example.com/icon-intermediate.png")
                .displayOrder(3)
                .isActive(true)
                .build();
        savedCategory.setCreatedAt(LocalDateTime.now());

        when(categoryRepository.existsByNameIgnoreCase("Intermediate")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

        // When
        CategoryResponse result = categoryService.createCategory(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getName()).isEqualTo("Intermediate");
        assertThat(result.getDescription()).isEqualTo("Intermediate level lessons");
        verify(categoryRepository, times(1)).existsByNameIgnoreCase("Intermediate");
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when creating category with existing name")
    void createCategory_DuplicateName() {
        // Given
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("Basics")
                .description("Duplicate category")
                .build();

        when(categoryRepository.existsByNameIgnoreCase("Basics")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Category already exists with name: 'Basics'");
        verify(categoryRepository, times(1)).existsByNameIgnoreCase("Basics");
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("Should update category successfully")
    void updateCategory_Success() {
        // Given
        UpdateCategoryRequest request = UpdateCategoryRequest.builder()
                .name("Updated Basics")
                .description("Updated description")
                .iconUrl("https://example.com/new-icon.png")
                .displayOrder(10)
                .isActive(false)
                .build();

        Category updatedCategory = Category.builder()
                .id(1L)
                .name("Updated Basics")
                .description("Updated description")
                .iconUrl("https://example.com/new-icon.png")
                .displayOrder(10)
                .isActive(false)
                .build();
        updatedCategory.setCreatedAt(LocalDateTime.now());

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));
        when(categoryRepository.existsByNameIgnoreCase("Updated Basics")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);

        // When
        CategoryResponse result = categoryService.updateCategory(1L, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated Basics");
        assertThat(result.getDescription()).isEqualTo("Updated description");
        assertThat(result.getIconUrl()).isEqualTo("https://example.com/new-icon.png");
        assertThat(result.getDisplayOrder()).isEqualTo(10);
        assertThat(result.getIsActive()).isFalse();
        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("Should update category with partial data")
    void updateCategory_PartialUpdate() {
        // Given
        UpdateCategoryRequest request = UpdateCategoryRequest.builder()
                .description("Only description updated")
                .build();

        Category updatedCategory = Category.builder()
                .id(1L)
                .name("Basics")
                .description("Only description updated")
                .iconUrl("https://example.com/icon-basics.png")
                .displayOrder(1)
                .isActive(true)
                .build();
        updatedCategory.setCreatedAt(LocalDateTime.now());

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));
        when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);

        // When
        CategoryResponse result = categoryService.updateCategory(1L, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Basics");
        assertThat(result.getDescription()).isEqualTo("Only description updated");
        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("Should throw exception when updating with duplicate name")
    void updateCategory_DuplicateName() {
        // Given
        UpdateCategoryRequest request = UpdateCategoryRequest.builder()
                .name("Advanced")
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));
        when(categoryRepository.existsByNameIgnoreCase("Advanced")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> categoryService.updateCategory(1L, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Category already exists with name: 'Advanced'");
        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent category")
    void updateCategory_NotFound() {
        // Given
        UpdateCategoryRequest request = UpdateCategoryRequest.builder()
                .name("Updated Name")
                .build();

        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryService.updateCategory(999L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found with id: '999'");
        verify(categoryRepository, times(1)).findById(999L);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("Should delete category successfully")
    void deleteCategory_Success() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));
        when(categoryRepository.countLessons(1L)).thenReturn(0L);
        doNothing().when(categoryRepository).delete(category1);

        // When
        categoryService.deleteCategory(1L);

        // Then
        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).countLessons(1L);
        verify(categoryRepository, times(1)).delete(category1);
    }

    @Test
    @DisplayName("Should throw exception when deleting category with lessons")
    void deleteCategory_HasLessons() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));
        when(categoryRepository.countLessons(1L)).thenReturn(5L);

        // When & Then
        assertThatThrownBy(() -> categoryService.deleteCategory(1L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Cannot delete category 'Basics' as it has 5 associated lessons");
        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).countLessons(1L);
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent category")
    void deleteCategory_NotFound() {
        // Given
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryService.deleteCategory(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found with id: '999'");
        verify(categoryRepository, times(1)).findById(999L);
        verify(categoryRepository, never()).countLessons(anyLong());
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    @DisplayName("Should get category with lessons successfully")
    void getCategoryWithLessons_Success() {
        // Given
        when(categoryRepository.findWithLessonsById(1L)).thenReturn(Optional.of(category1));

        // When
        Category result = categoryService.getCategoryWithLessons(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Basics");
        verify(categoryRepository, times(1)).findWithLessonsById(1L);
    }

    @Test
    @DisplayName("Should throw exception when getting category with lessons not found")
    void getCategoryWithLessons_NotFound() {
        // Given
        when(categoryRepository.findWithLessonsById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryService.getCategoryWithLessons(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found with id: '999'");
        verify(categoryRepository, times(1)).findWithLessonsById(999L);
    }

    @Test
    @DisplayName("Should get lesson count for category")
    void getLessonCount_Success() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));
        when(categoryRepository.countLessons(1L)).thenReturn(10L);

        // When
        long count = categoryService.getLessonCount(1L);

        // Then
        assertThat(count).isEqualTo(10L);
        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).countLessons(1L);
    }

    @Test
    @DisplayName("Should throw exception when getting lesson count for non-existent category")
    void getLessonCount_CategoryNotFound() {
        // Given
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryService.getLessonCount(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found with id: '999'");
        verify(categoryRepository, times(1)).findById(999L);
        verify(categoryRepository, never()).countLessons(anyLong());
    }

    @Test
    @DisplayName("Should handle empty category list")
    void getAllCategories_EmptyList() {
        // Given
        when(categoryRepository.findAllByOrderByDisplayOrderAsc()).thenReturn(Arrays.asList());

        // When
        List<CategoryResponse> result = categoryService.getAllCategories();

        // Then
        assertThat(result).isEmpty();
        verify(categoryRepository, times(1)).findAllByOrderByDisplayOrderAsc();
    }

    @Test
    @DisplayName("Should allow updating category with same name (case insensitive)")
    void updateCategory_SameNameDifferentCase() {
        // Given
        UpdateCategoryRequest request = UpdateCategoryRequest.builder()
                .name("BASICS")
                .build();

        Category updatedCategory = Category.builder()
                .id(1L)
                .name("BASICS")
                .description("Basic Morse code lessons")
                .iconUrl("https://example.com/icon-basics.png")
                .displayOrder(1)
                .isActive(true)
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));
        when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);

        // When
        CategoryResponse result = categoryService.updateCategory(1L, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("BASICS");
        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).save(any(Category.class));
        verify(categoryRepository, never()).existsByNameIgnoreCase(anyString());
    }
}
