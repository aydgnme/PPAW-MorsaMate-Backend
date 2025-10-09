package me.aydgn.MorseMate.repository;

import me.aydgn.MorseMate.entity.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("CategoryRepository Tests")
class CategoryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category activeCategory1;
    private Category activeCategory2;
    private Category inactiveCategory;

    @BeforeEach
    void setUp() {
        // Clear any existing data
        categoryRepository.deleteAll();
        entityManager.flush();

        // Create test categories
        activeCategory1 = Category.builder()
                .name("Basics")
                .description("Basic Morse code lessons")
                .displayOrder(1)
                .iconUrl("https://example.com/icons/basics.png")
                .isActive(true)
                .build();

        activeCategory2 = Category.builder()
                .name("Advanced")
                .description("Advanced Morse code lessons")
                .displayOrder(2)
                .iconUrl("https://example.com/icons/advanced.png")
                .isActive(true)
                .build();

        inactiveCategory = Category.builder()
                .name("Archived")
                .description("Archived lessons")
                .displayOrder(3)
                .iconUrl("https://example.com/icons/archived.png")
                .isActive(false)
                .build();

        // Persist test data
        entityManager.persist(activeCategory1);
        entityManager.persist(activeCategory2);
        entityManager.persist(inactiveCategory);
        entityManager.flush();
    }

    @Test
    @DisplayName("Should save and find category by ID")
    void saveAndFindById() {
        // Given
        Category newCategory = Category.builder()
                .name("Intermediate")
                .description("Intermediate level")
                .displayOrder(4)
                .iconUrl("https://example.com/icons/intermediate.png")
                .isActive(true)
                .build();

        // When
        Category saved = categoryRepository.save(newCategory);
        Optional<Category> found = categoryRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Intermediate");
        assertThat(found.get().getDescription()).isEqualTo("Intermediate level");
        assertThat(found.get().getDisplayOrder()).isEqualTo(4);
        assertThat(found.get().getIconUrl()).isEqualTo("https://example.com/icons/intermediate.png");
        assertThat(found.get().getIsActive()).isTrue();
        assertThat(found.get().getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should find all active categories")
    void findByIsActiveTrue() {
        // When
        List<Category> activeCategories = categoryRepository.findByIsActiveTrue();

        // Then
        assertThat(activeCategories).hasSize(2);
        assertThat(activeCategories)
                .extracting(Category::getName)
                .containsExactlyInAnyOrder("Basics", "Advanced");
        assertThat(activeCategories)
                .allMatch(Category::getIsActive);
    }

    @Test
    @DisplayName("Should find all categories ordered by display order")
    void findAllByOrderByDisplayOrderAsc() {
        // When
        List<Category> categories = categoryRepository.findAllByOrderByDisplayOrderAsc();

        // Then
        assertThat(categories).hasSize(3);
        assertThat(categories.get(0).getName()).isEqualTo("Basics");
        assertThat(categories.get(0).getDisplayOrder()).isEqualTo(1);
        assertThat(categories.get(1).getName()).isEqualTo("Advanced");
        assertThat(categories.get(1).getDisplayOrder()).isEqualTo(2);
        assertThat(categories.get(2).getName()).isEqualTo("Archived");
        assertThat(categories.get(2).getDisplayOrder()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should find active categories ordered by display order")
    void findByIsActiveTrueOrderByDisplayOrderAsc() {
        // When
        List<Category> categories = categoryRepository.findByIsActiveTrueOrderByDisplayOrderAsc();

        // Then
        assertThat(categories).hasSize(2);
        assertThat(categories.get(0).getName()).isEqualTo("Basics");
        assertThat(categories.get(0).getDisplayOrder()).isEqualTo(1);
        assertThat(categories.get(0).getIsActive()).isTrue();
        assertThat(categories.get(1).getName()).isEqualTo("Advanced");
        assertThat(categories.get(1).getDisplayOrder()).isEqualTo(2);
        assertThat(categories.get(1).getIsActive()).isTrue();
    }

    @Test
    @DisplayName("Should find category by name (case insensitive)")
    void findByNameIgnoreCase() {
        // When
        Optional<Category> foundLowerCase = categoryRepository.findByNameIgnoreCase("basics");
        Optional<Category> foundUpperCase = categoryRepository.findByNameIgnoreCase("BASICS");
        Optional<Category> foundMixedCase = categoryRepository.findByNameIgnoreCase("BaSiCs");
        Optional<Category> notFound = categoryRepository.findByNameIgnoreCase("nonexistent");

        // Then
        assertThat(foundLowerCase).isPresent();
        assertThat(foundLowerCase.get().getName()).isEqualTo("Basics");
        assertThat(foundUpperCase).isPresent();
        assertThat(foundUpperCase.get().getName()).isEqualTo("Basics");
        assertThat(foundMixedCase).isPresent();
        assertThat(foundMixedCase.get().getName()).isEqualTo("Basics");
        assertThat(notFound).isEmpty();
    }

    @Test
    @DisplayName("Should check if category exists by name (case insensitive)")
    void existsByNameIgnoreCase() {
        // When & Then
        assertThat(categoryRepository.existsByNameIgnoreCase("basics")).isTrue();
        assertThat(categoryRepository.existsByNameIgnoreCase("BASICS")).isTrue();
        assertThat(categoryRepository.existsByNameIgnoreCase("Advanced")).isTrue();
        assertThat(categoryRepository.existsByNameIgnoreCase("nonexistent")).isFalse();
    }

    @Test
    @DisplayName("Should find all categories ordered by name")
    void findAllByOrderByNameAsc() {
        // When
        List<Category> categories = categoryRepository.findAllByOrderByNameAsc();

        // Then
        assertThat(categories).hasSize(3);
        assertThat(categories.get(0).getName()).isEqualTo("Advanced");
        assertThat(categories.get(1).getName()).isEqualTo("Archived");
        assertThat(categories.get(2).getName()).isEqualTo("Basics");
    }

    @Test
    @DisplayName("Should update category fields")
    void updateCategory() {
        // Given
        Category category = categoryRepository.findByNameIgnoreCase("Basics").orElseThrow();
        category.setDescription("Updated description");
        category.setDisplayOrder(10);
        category.setIsActive(false);

        // When
        Category updated = categoryRepository.save(category);

        // Then
        assertThat(updated.getDescription()).isEqualTo("Updated description");
        assertThat(updated.getDisplayOrder()).isEqualTo(10);
        assertThat(updated.getIsActive()).isFalse();
        assertThat(updated.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should delete category")
    void deleteCategory() {
        // Given
        Category category = categoryRepository.findByNameIgnoreCase("Archived").orElseThrow();
        Long categoryId = category.getId();

        // When
        categoryRepository.delete(category);
        entityManager.flush();

        // Then
        Optional<Category> deleted = categoryRepository.findById(categoryId);
        assertThat(deleted).isEmpty();
        assertThat(categoryRepository.findAll()).hasSize(2);
    }

    @Test
    @DisplayName("Should count all categories")
    void countCategories() {
        // When
        long count = categoryRepository.count();

        // Then
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("Should set isActive to true by default")
    void defaultIsActiveTrue() {
        // Given
        Category category = Category.builder()
                .name("New Category")
                .description("Test default isActive")
                .displayOrder(5)
                .build();

        // When
        Category saved = categoryRepository.save(category);

        // Then
        assertThat(saved.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("Should handle null displayOrder")
    void handleNullDisplayOrder() {
        // Given
        Category category = Category.builder()
                .name("No Order")
                .description("Category without display order")
                .isActive(true)
                .build();

        // When
        Category saved = categoryRepository.save(category);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getDisplayOrder()).isNull();
    }

    @Test
    @DisplayName("Should trim whitespace from name and iconUrl")
    void trimWhitespace() {
        // Given
        Category category = Category.builder()
                .name("  Trimmed  ")
                .description("Test trimming")
                .iconUrl("  https://example.com/icon.png  ")
                .displayOrder(6)
                .isActive(true)
                .build();

        // When
        Category saved = categoryRepository.save(category);
        entityManager.flush();
        entityManager.clear();

        // Then
        Category found = categoryRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getName()).isEqualTo("Trimmed");
        assertThat(found.getIconUrl()).isEqualTo("https://example.com/icon.png");
    }

    @Test
    @DisplayName("Should return empty list when no active categories exist")
    void findByIsActiveTrue_EmptyResult() {
        // Given - set all categories to inactive
        categoryRepository.findAll().forEach(category -> {
            category.setIsActive(false);
            categoryRepository.save(category);
        });
        entityManager.flush();

        // When
        List<Category> activeCategories = categoryRepository.findByIsActiveTrue();

        // Then
        assertThat(activeCategories).isEmpty();
    }

    @Test
    @DisplayName("Should handle categories with same display order")
    void handleSameDisplayOrder() {
        // Given
        Category category1 = Category.builder()
                .name("Category A")
                .displayOrder(10)
                .isActive(true)
                .build();

        Category category2 = Category.builder()
                .name("Category B")
                .displayOrder(10)
                .isActive(true)
                .build();

        // When
        categoryRepository.save(category1);
        categoryRepository.save(category2);
        entityManager.flush();

        List<Category> categories = categoryRepository.findAllByOrderByDisplayOrderAsc();

        // Then
        assertThat(categories).hasSizeGreaterThanOrEqualTo(2);
        // Both categories with order 10 should be present
        assertThat(categories)
                .filteredOn(cat -> cat.getDisplayOrder() != null && cat.getDisplayOrder() == 10)
                .hasSize(2);
    }

    @Test
    @DisplayName("Should persist and retrieve createdAt timestamp")
    void persistCreatedAt() {
        // Given
        Category category = Category.builder()
                .name("Timestamped")
                .description("Test timestamps")
                .displayOrder(7)
                .isActive(true)
                .build();

        // When
        Category saved = categoryRepository.save(category);
        entityManager.flush();
        entityManager.clear();

        // Then
        Category found = categoryRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
    }
}
