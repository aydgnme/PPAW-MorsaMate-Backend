package me.aydgn.MorseMate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.aydgn.MorseMate.dto.request.CreateCategoryRequest;
import me.aydgn.MorseMate.dto.request.UpdateCategoryRequest;
import me.aydgn.MorseMate.dto.response.CategoryResponse;
import me.aydgn.MorseMate.exception.DuplicateResourceException;
import me.aydgn.MorseMate.exception.GlobalExceptionHandler;
import me.aydgn.MorseMate.exception.InvalidOperationException;
import me.aydgn.MorseMate.exception.ResourceNotFoundException;
import me.aydgn.MorseMate.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CategoryController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
                org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class,
                org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration.class
        },
        excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
                type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                classes = {
                        me.aydgn.MorseMate.config.SecurityConfig.class,
                        me.aydgn.MorseMate.config.JwtAuthenticationFilter.class,
                        me.aydgn.MorseMate.config.JpaConfig.class
                }
        ))
@Import(GlobalExceptionHandler.class)
@DisplayName("CategoryController Integration Tests")
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    private CategoryResponse category1;
    private CategoryResponse category2;
    private CreateCategoryRequest createRequest;
    private UpdateCategoryRequest updateRequest;

    @BeforeEach
    void setUp() {
        category1 = CategoryResponse.builder()
                .id(1L)
                .name("Basics")
                .description("Basic Morse code lessons")
                .iconUrl("https://example.com/icon-basics.png")
                .displayOrder(1)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        category2 = CategoryResponse.builder()
                .id(2L)
                .name("Advanced")
                .description("Advanced Morse code lessons")
                .iconUrl("https://example.com/icon-advanced.png")
                .displayOrder(2)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        createRequest = CreateCategoryRequest.builder()
                .name("Intermediate")
                .description("Intermediate level lessons")
                .iconUrl("https://example.com/icon-intermediate.png")
                .displayOrder(3)
                .isActive(true)
                .build();

        updateRequest = UpdateCategoryRequest.builder()
                .name("Updated Basics")
                .description("Updated description")
                .displayOrder(10)
                .isActive(false)
                .build();
    }

    // ================ GET /v1/categories Tests ================

    @Test
    @DisplayName("GET /v1/categories - Should return all active categories")
    void getAllCategories_Success() throws Exception {
        // Given
        List<CategoryResponse> categories = Arrays.asList(category1, category2);
        when(categoryService.getActiveCategories()).thenReturn(categories);

        // When & Then
        mockMvc.perform(get("/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Basics"))
                .andExpect(jsonPath("$[0].description").value("Basic Morse code lessons"))
                .andExpect(jsonPath("$[0].displayOrder").value(1))
                .andExpect(jsonPath("$[0].isActive").value(true))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Advanced"));

        verify(categoryService, times(1)).getActiveCategories();
    }

    @Test
    @DisplayName("GET /v1/categories - Should return empty list when no categories")
    void getAllCategories_EmptyList() throws Exception {
        // Given
        when(categoryService.getActiveCategories()).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(categoryService, times(1)).getActiveCategories();
    }

    // ================ GET /v1/categories/{id} Tests ================

    @Test
    @DisplayName("GET /v1/categories/{id} - Should return category by ID")
    void getCategoryById_Success() throws Exception {
        // Given
        when(categoryService.getCategoryById(1L)).thenReturn(category1);

        // When & Then
        mockMvc.perform(get("/v1/categories/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Basics"))
                .andExpect(jsonPath("$.description").value("Basic Morse code lessons"))
                .andExpect(jsonPath("$.displayOrder").value(1))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(categoryService, times(1)).getCategoryById(1L);
    }

    @Test
    @DisplayName("GET /v1/categories/{id} - Should return 404 when category not found")
    void getCategoryById_NotFound() throws Exception {
        // Given
        when(categoryService.getCategoryById(999L))
                .thenThrow(new ResourceNotFoundException("Category", "id", 999L));

        // When & Then
        mockMvc.perform(get("/v1/categories/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Category not found with id: '999'"));

        verify(categoryService, times(1)).getCategoryById(999L);
    }

    // ================ POST /v1/categories Tests ================

    @Test
    @DisplayName("POST /v1/categories - Should create category successfully")
    void createCategory_Success() throws Exception {
        // Given
        CategoryResponse createdCategory = CategoryResponse.builder()
                .id(3L)
                .name("Intermediate")
                .description("Intermediate level lessons")
                .iconUrl("https://example.com/icon-intermediate.png")
                .displayOrder(3)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(categoryService.createCategory(any(CreateCategoryRequest.class)))
                .thenReturn(createdCategory);

        // When & Then
        mockMvc.perform(post("/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("Intermediate"))
                .andExpect(jsonPath("$.description").value("Intermediate level lessons"));

        verify(categoryService, times(1)).createCategory(any(CreateCategoryRequest.class));
    }

    @Test
    @DisplayName("POST /v1/categories - Should return 400 when name is blank")
    void createCategory_BlankName_BadRequest() throws Exception {
        // Given
        CreateCategoryRequest invalidRequest = CreateCategoryRequest.builder()
                .name("")
                .build();

        // When & Then
        mockMvc.perform(post("/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).createCategory(any(CreateCategoryRequest.class));
    }

    @Test
    @DisplayName("POST /v1/categories - Should return 400 when name exceeds max length")
    void createCategory_NameTooLong_BadRequest() throws Exception {
        // Given
        CreateCategoryRequest invalidRequest = CreateCategoryRequest.builder()
                .name("A".repeat(101))
                .build();

        // When & Then
        mockMvc.perform(post("/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).createCategory(any(CreateCategoryRequest.class));
    }

    @Test
    @DisplayName("POST /v1/categories - Should return 400 when URL has invalid protocol")
    void createCategory_InvalidUrlProtocol_BadRequest() throws Exception {
        // Given
        CreateCategoryRequest invalidRequest = CreateCategoryRequest.builder()
                .name("Valid Name")
                .iconUrl("ftp://example.com/icon.png")
                .build();

        // When & Then
        mockMvc.perform(post("/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).createCategory(any(CreateCategoryRequest.class));
    }

    @Test
    @DisplayName("POST /v1/categories - Should return 409 when category name already exists")
    void createCategory_DuplicateName_Conflict() throws Exception {
        // Given
        when(categoryService.createCategory(any(CreateCategoryRequest.class)))
                .thenThrow(new DuplicateResourceException("Category", "name", "Basics"));

        // When & Then
        mockMvc.perform(post("/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Category already exists with name: 'Basics'"));

        verify(categoryService, times(1)).createCategory(any(CreateCategoryRequest.class));
    }

    // ================ PUT /v1/categories/{id} Tests ================

    @Test
    @DisplayName("PUT /v1/categories/{id} - Should update category successfully")
    void updateCategory_Success() throws Exception {
        // Given
        CategoryResponse updatedCategory = CategoryResponse.builder()
                .id(1L)
                .name("Updated Basics")
                .description("Updated description")
                .displayOrder(10)
                .isActive(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(categoryService.updateCategory(eq(1L), any(UpdateCategoryRequest.class)))
                .thenReturn(updatedCategory);

        // When & Then
        mockMvc.perform(put("/v1/categories/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Basics"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.displayOrder").value(10))
                .andExpect(jsonPath("$.isActive").value(false));

        verify(categoryService, times(1)).updateCategory(eq(1L), any(UpdateCategoryRequest.class));
    }

    @Test
    @DisplayName("PUT /v1/categories/{id} - Should return 404 when category not found")
    void updateCategory_NotFound() throws Exception {
        // Given
        when(categoryService.updateCategory(eq(999L), any(UpdateCategoryRequest.class)))
                .thenThrow(new ResourceNotFoundException("Category", "id", 999L));

        // When & Then
        mockMvc.perform(put("/v1/categories/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Category not found with id: '999'"));

        verify(categoryService, times(1)).updateCategory(eq(999L), any(UpdateCategoryRequest.class));
    }

    @Test
    @DisplayName("PUT /v1/categories/{id} - Should return 409 when updating to existing name")
    void updateCategory_DuplicateName_Conflict() throws Exception {
        // Given
        when(categoryService.updateCategory(eq(1L), any(UpdateCategoryRequest.class)))
                .thenThrow(new DuplicateResourceException("Category", "name", "Advanced"));

        // When & Then
        mockMvc.perform(put("/v1/categories/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Category already exists with name: 'Advanced'"));

        verify(categoryService, times(1)).updateCategory(eq(1L), any(UpdateCategoryRequest.class));
    }

    @Test
    @DisplayName("PUT /v1/categories/{id} - Should update with partial data")
    void updateCategory_PartialUpdate_Success() throws Exception {
        // Given
        UpdateCategoryRequest partialRequest = UpdateCategoryRequest.builder()
                .description("Only description updated")
                .build();

        CategoryResponse updatedCategory = CategoryResponse.builder()
                .id(1L)
                .name("Basics")
                .description("Only description updated")
                .displayOrder(1)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(categoryService.updateCategory(eq(1L), any(UpdateCategoryRequest.class)))
                .thenReturn(updatedCategory);

        // When & Then
        mockMvc.perform(put("/v1/categories/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partialRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Basics"))
                .andExpect(jsonPath("$.description").value("Only description updated"));

        verify(categoryService, times(1)).updateCategory(eq(1L), any(UpdateCategoryRequest.class));
    }

    // ================ DELETE /v1/categories/{id} Tests ================

    @Test
    @DisplayName("DELETE /v1/categories/{id} - Should delete category successfully")
    void deleteCategory_Success() throws Exception {
        // Given
        doNothing().when(categoryService).deleteCategory(1L);

        // When & Then
        mockMvc.perform(delete("/v1/categories/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Category deleted successfully"));

        verify(categoryService, times(1)).deleteCategory(1L);
    }

    @Test
    @DisplayName("DELETE /v1/categories/{id} - Should return 404 when category not found")
    void deleteCategory_NotFound() throws Exception {
        // Given
        doThrow(new ResourceNotFoundException("Category", "id", 999L))
                .when(categoryService).deleteCategory(999L);

        // When & Then
        mockMvc.perform(delete("/v1/categories/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Category not found with id: '999'"));

        verify(categoryService, times(1)).deleteCategory(999L);
    }

    @Test
    @DisplayName("DELETE /v1/categories/{id} - Should return 400 when category has lessons")
    void deleteCategory_HasLessons_BadRequest() throws Exception {
        // Given
        doThrow(new InvalidOperationException("Cannot delete category 'Basics' as it has 5 associated lessons"))
                .when(categoryService).deleteCategory(1L);

        // When & Then
        mockMvc.perform(delete("/v1/categories/{id}", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot delete category 'Basics' as it has 5 associated lessons"));

        verify(categoryService, times(1)).deleteCategory(1L);
    }

    // ================ GET /v1/categories/admin/all Tests ================

    @Test
    @DisplayName("GET /v1/categories/admin/all - Should get all categories including inactive")
    void getAllCategoriesForAdmin_Success() throws Exception {
        // Given
        CategoryResponse inactiveCategory = CategoryResponse.builder()
                .id(3L)
                .name("Inactive Category")
                .description("This is inactive")
                .displayOrder(3)
                .isActive(false)
                .createdAt(LocalDateTime.now())
                .build();

        List<CategoryResponse> allCategories = Arrays.asList(category1, category2, inactiveCategory);
        when(categoryService.getAllCategories()).thenReturn(allCategories);

        // When & Then
        mockMvc.perform(get("/v1/categories/admin/all"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].isActive").value(true))
                .andExpect(jsonPath("$[1].isActive").value(true))
                .andExpect(jsonPath("$[2].isActive").value(false));

        verify(categoryService, times(1)).getAllCategories();
    }
}
