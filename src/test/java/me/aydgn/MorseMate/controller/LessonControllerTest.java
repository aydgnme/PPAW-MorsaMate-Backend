package me.aydgn.MorseMate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.aydgn.MorseMate.dto.request.CreateLessonRequest;
import me.aydgn.MorseMate.dto.request.UpdateLessonRequest;
import me.aydgn.MorseMate.dto.response.LessonResponse;
import me.aydgn.MorseMate.exception.GlobalExceptionHandler;
import me.aydgn.MorseMate.exception.InvalidOperationException;
import me.aydgn.MorseMate.exception.ResourceNotFoundException;
import me.aydgn.MorseMate.service.LessonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

@WebMvcTest(controllers = LessonController.class,
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
@DisplayName("LessonController Integration Tests")
class LessonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LessonService lessonService;

    private LessonResponse lesson1;
    private LessonResponse lesson2;
    private CreateLessonRequest createRequest;
    private UpdateLessonRequest updateRequest;

    @BeforeEach
    void setUp() {
        lesson1 = LessonResponse.builder()
                .id(1L)
                .categoryId(1L)
                .categoryName("Basics")
                .title("Introduction to Morse Code")
                .description("Learn the basics of Morse code")
                .difficulty("BEGINNER")
                .content("This lesson covers...")
                .orderIndex(1)
                .pointsReward(10)
                .estimatedDuration(15)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        lesson2 = LessonResponse.builder()
                .id(2L)
                .categoryId(1L)
                .categoryName("Basics")
                .title("Morse Code Alphabet")
                .description("Master the Morse code alphabet")
                .difficulty("BEGINNER")
                .content("A complete guide to...")
                .orderIndex(2)
                .pointsReward(15)
                .estimatedDuration(20)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createRequest = CreateLessonRequest.builder()
                .categoryId(1L)
                .title("Advanced Techniques")
                .description("Advanced Morse code techniques")
                .difficulty("ADVANCED")
                .content("This advanced lesson...")
                .orderIndex(3)
                .pointsReward(20)
                .estimatedDuration(30)
                .build();

        updateRequest = UpdateLessonRequest.builder()
                .id(1L)
                .title("Updated Title")
                .description("Updated description")
                .difficulty("INTERMEDIATE")
                .orderIndex(10)
                .pointsReward(25)
                .build();
    }

    @Test
    @DisplayName("GET /v1/lessons/{id} - Success")
    void getLessonById_Success() throws Exception {
        // Given
        when(lessonService.getLessonById(1L, false)).thenReturn(lesson1);

        // When & Then
        mockMvc.perform(get("/v1/lessons/1")
                        .param("includeExercises", "false"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Introduction to Morse Code")))
                .andExpect(jsonPath("$.categoryId", is(1)))
                .andExpect(jsonPath("$.difficulty", is("BEGINNER")))
                .andExpect(jsonPath("$.pointsReward", is(10)));

        verify(lessonService, times(1)).getLessonById(1L, false);
    }

    @Test
    @DisplayName("GET /v1/lessons/{id} - Not Found")
    void getLessonById_NotFound() throws Exception {
        // Given
        when(lessonService.getLessonById(999L, false))
                .thenThrow(new ResourceNotFoundException("Lesson", "id", 999L));

        // When & Then
        mockMvc.perform(get("/v1/lessons/999")
                        .param("includeExercises", "false"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Lesson not found")));

        verify(lessonService, times(1)).getLessonById(999L, false);
    }

    @Test
    @DisplayName("GET /v1/lessons?categoryId={id} - Success")
    void getLessonsByCategoryId_Success() throws Exception {
        // Given
        List<LessonResponse> lessons = Arrays.asList(lesson1, lesson2);
        when(lessonService.getLessonsByCategoryId(1L)).thenReturn(lessons);

        // When & Then
        mockMvc.perform(get("/v1/lessons")
                        .param("categoryId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Introduction to Morse Code")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].title", is("Morse Code Alphabet")));

        verify(lessonService, times(1)).getLessonsByCategoryId(1L);
    }

    @Test
    @DisplayName("GET /v1/lessons/search?title={title} - Success")
    void searchLessonsByTitle_Success() throws Exception {
        // Given
        List<LessonResponse> lessons = Arrays.asList(lesson1);
        Page<LessonResponse> page = new PageImpl<>(lessons, PageRequest.of(0, 20), 1);
        when(lessonService.searchLessonsByTitle(eq("Introduction"), any(Pageable.class)))
                .thenReturn(page);

        // When & Then
        mockMvc.perform(get("/v1/lessons/search")
                        .param("title", "Introduction"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", is("Introduction to Morse Code")))
                .andExpect(jsonPath("$.totalElements", is(1)));

        verify(lessonService, times(1)).searchLessonsByTitle(eq("Introduction"), any(Pageable.class));
    }

    @Test
    @DisplayName("POST /v1/lessons - Success")
    void createLesson_Success() throws Exception {
        // Given
        LessonResponse createdLesson = LessonResponse.builder()
                .id(3L)
                .categoryId(createRequest.getCategoryId())
                .title(createRequest.getTitle())
                .description(createRequest.getDescription())
                .difficulty(createRequest.getDifficulty())
                .content(createRequest.getContent())
                .orderIndex(createRequest.getOrderIndex())
                .pointsReward(createRequest.getPointsReward())
                .estimatedDuration(createRequest.getEstimatedDuration())
                .createdAt(LocalDateTime.now())
                .build();

        when(lessonService.createLesson(any(CreateLessonRequest.class))).thenReturn(createdLesson);

        // When & Then
        mockMvc.perform(post("/v1/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.title", is("Advanced Techniques")))
                .andExpect(jsonPath("$.difficulty", is("ADVANCED")))
                .andExpect(jsonPath("$.pointsReward", is(20)));

        verify(lessonService, times(1)).createLesson(any(CreateLessonRequest.class));
    }

    @Test
    @DisplayName("POST /v1/lessons - Invalid Request")
    void createLesson_InvalidRequest() throws Exception {
        // Given - missing required fields
        CreateLessonRequest invalidRequest = CreateLessonRequest.builder()
                .title("") // blank title
                .build();

        // When & Then
        mockMvc.perform(post("/v1/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(lessonService, never()).createLesson(any(CreateLessonRequest.class));
    }

    @Test
    @DisplayName("PUT /v1/lessons/{id} - Success")
    void updateLesson_Success() throws Exception {
        // Given
        LessonResponse updatedLesson = LessonResponse.builder()
                .id(1L)
                .title(updateRequest.getTitle())
                .description(updateRequest.getDescription())
                .difficulty(updateRequest.getDifficulty())
                .orderIndex(updateRequest.getOrderIndex())
                .pointsReward(updateRequest.getPointsReward())
                .build();

        when(lessonService.updateLesson(eq(1L), any(UpdateLessonRequest.class))).thenReturn(updatedLesson);

        // When & Then
        mockMvc.perform(put("/v1/lessons/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Updated Title")))
                .andExpect(jsonPath("$.difficulty", is("INTERMEDIATE")));

        verify(lessonService, times(1)).updateLesson(eq(1L), any(UpdateLessonRequest.class));
    }

    @Test
    @DisplayName("DELETE /v1/lessons/{id} - Success")
    void deleteLesson_Success() throws Exception {
        // Given
        doNothing().when(lessonService).deleteLesson(1L);

        // When & Then
        mockMvc.perform(delete("/v1/lessons/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Lesson deleted successfully")));

        verify(lessonService, times(1)).deleteLesson(1L);
    }

    @Test
    @DisplayName("DELETE /v1/lessons/{id} - Has Associated Exercises")
    void deleteLesson_HasExercises() throws Exception {
        // Given
        doThrow(new InvalidOperationException("Cannot delete lesson as it has associated exercises"))
                .when(lessonService).deleteLesson(1L);

        // When & Then
        mockMvc.perform(delete("/v1/lessons/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Cannot delete lesson")));

        verify(lessonService, times(1)).deleteLesson(1L);
    }

    @Test
    @DisplayName("GET /v1/lessons/{id}/exercise-count - Success")
    void getExerciseCount_Success() throws Exception {
        // Given
        when(lessonService.getExerciseCount(1L)).thenReturn(5L);

        // When & Then
        mockMvc.perform(get("/v1/lessons/1/exercise-count"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("5"));

        verify(lessonService, times(1)).getExerciseCount(1L);
    }

    @Test
    @DisplayName("PATCH /v1/lessons/{id}/order - Success")
    void updateLessonOrder_Success() throws Exception {
        // Given
        doNothing().when(lessonService).updateLessonOrder(1L, 10);

        // When & Then
        mockMvc.perform(patch("/v1/lessons/1/order")
                        .param("orderIndex", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Lesson order updated successfully")));

        verify(lessonService, times(1)).updateLessonOrder(1L, 10);
    }
}
