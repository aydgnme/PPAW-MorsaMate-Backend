package me.aydgn.MorseMate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.aydgn.MorseMate.dto.request.MarkLessonCompletedRequest;
import me.aydgn.MorseMate.dto.response.UserProgressResponse;
import me.aydgn.MorseMate.exception.GlobalExceptionHandler;
import me.aydgn.MorseMate.exception.ResourceNotFoundException;
import me.aydgn.MorseMate.service.UserProgressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserProgressController.class,
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
@DisplayName("UserProgressController Integration Tests")
class UserProgressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserProgressService userProgressService;

    private UserProgressResponse progress1;
    private UserProgressResponse progress2;
    private MarkLessonCompletedRequest completedRequest;

    @BeforeEach
    void setUp() {
        progress1 = UserProgressResponse.builder()
                .id(1L)
                .userId(1L)
                .lessonId(1L)
                .lessonTitle("Introduction to Morse Code")
                .attempts(3)
                .isCompleted(true)
                .score(95)
                .starsEarned(3)
                .timeSpent(300)
                .completedAt(LocalDateTime.now())
                .build();

        progress2 = UserProgressResponse.builder()
                .id(2L)
                .userId(1L)
                .lessonId(2L)
                .lessonTitle("Morse Code Alphabet")
                .attempts(1)
                .isCompleted(false)
                .build();

        completedRequest = MarkLessonCompletedRequest.builder()
                .score(95)
                .starsEarned(3)
                .timeSpent(300)
                .build();
    }

    @Test
    @DisplayName("GET /v1/progress/me - Success")
    void getMyProgress_Success() throws Exception {
        List<UserProgressResponse> progressList = Arrays.asList(progress1, progress2);
        when(userProgressService.getAllUserProgress(1L)).thenReturn(progressList);

        mockMvc.perform(get("/v1/progress/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));

        verify(userProgressService, times(1)).getAllUserProgress(1L);
    }

    @Test
    @DisplayName("GET /v1/progress/me/paged - Success")
    void getMyProgressPaged_Success() throws Exception {
        Page<UserProgressResponse> page = new PageImpl<>(Arrays.asList(progress1, progress2));
        when(userProgressService.getAllUserProgressPaged(eq(1L), any()))
                .thenReturn(page);

        mockMvc.perform(get("/v1/progress/me/paged"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].isCompleted", is(true)))
                .andExpect(jsonPath("$.content[1].isCompleted", is(false)));

        verify(userProgressService, times(1)).getAllUserProgressPaged(eq(1L), any());
    }

    @Test
    @DisplayName("GET /v1/progress/me/lessons/{lessonId} - Success")
    void getMyLessonProgress_Success() throws Exception {
        when(userProgressService.getUserProgressForLesson(1L, 1L)).thenReturn(progress1);

        mockMvc.perform(get("/v1/progress/me/lessons/{lessonId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.lessonId", is(1)))
                .andExpect(jsonPath("$.isCompleted", is(true)))
                .andExpect(jsonPath("$.score", is(95)));

        verify(userProgressService, times(1)).getUserProgressForLesson(1L, 1L);
    }

    @Test
    @DisplayName("GET /v1/progress/me/lessons/{lessonId} - Not Found")
    void getMyLessonProgress_NotFound() throws Exception {
        when(userProgressService.getUserProgressForLesson(1L, 999L))
                .thenThrow(new ResourceNotFoundException("UserProgress", "lessonId", 999L));

        mockMvc.perform(get("/v1/progress/me/lessons/{lessonId}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /v1/progress/me/statistics - Success")
    void getMyStatistics_Success() throws Exception {
        Map<String, Object> stats = new HashMap<>();
        stats.put("completedLessons", 5L);
        stats.put("totalAttempts", 15);
        stats.put("totalScore", 450);
        stats.put("averageScore", 90.0);
        stats.put("totalStars", 14);

        when(userProgressService.getUserStatistics(1L)).thenReturn(stats);

        mockMvc.perform(get("/v1/progress/me/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completedLessons", is(5)))
                .andExpect(jsonPath("$.totalAttempts", is(15)))
                .andExpect(jsonPath("$.averageScore", is(90.0)));

        verify(userProgressService, times(1)).getUserStatistics(1L);
    }

    @Test
    @DisplayName("POST /v1/progress/me/lessons/{lessonId}/increment - Success")
    void incrementAttempts_Success() throws Exception {
        UserProgressResponse updated = UserProgressResponse.builder()
                .id(2L)
                .userId(1L)
                .lessonId(2L)
                .lessonTitle("Morse Code Alphabet")
                .attempts(2)
                .isCompleted(false)
                .build();
        when(userProgressService.incrementAttempts(1L, 2L)).thenReturn(updated);

        mockMvc.perform(post("/v1/progress/me/lessons/{lessonId}/increment", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lessonId", is(2)))
                .andExpect(jsonPath("$.attempts", is(2)));

        verify(userProgressService, times(1)).incrementAttempts(1L, 2L);
    }

    @Test
    @DisplayName("POST /v1/progress/me/lessons/{lessonId}/complete - Success")
    void markLessonCompleted_Success() throws Exception {
        when(userProgressService.markLessonCompleted(eq(1L), eq(1L), anyInt(), anyInt(), anyInt()))
                .thenReturn(progress1);

        mockMvc.perform(post("/v1/progress/me/lessons/{lessonId}/complete", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(completedRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isCompleted", is(true)))
                .andExpect(jsonPath("$.score", is(95)))
                .andExpect(jsonPath("$.starsEarned", is(3)));

        verify(userProgressService, times(1))
                .markLessonCompleted(eq(1L), eq(1L), anyInt(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("POST /v1/progress/me/lessons/{lessonId}/complete - Validation Error")
    void markLessonCompleted_ValidationError() throws Exception {
        MarkLessonCompletedRequest invalidRequest = MarkLessonCompletedRequest.builder().build();

        mockMvc.perform(post("/v1/progress/me/lessons/{lessonId}/complete", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /v1/progress/me/lessons/{lessonId}/reset - Success")
    void resetLessonCompletion_Success() throws Exception {
        UserProgressResponse reset = UserProgressResponse.builder()
                .id(1L)
                .userId(1L)
                .lessonId(1L)
                .lessonTitle("Introduction to Morse Code")
                .attempts(3)
                .isCompleted(false)
                .build();
        when(userProgressService.resetLessonCompletion(1L, 1L)).thenReturn(reset);

        mockMvc.perform(post("/v1/progress/me/lessons/{lessonId}/reset", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isCompleted", is(false)))
                .andExpect(jsonPath("$.score").doesNotExist());

        verify(userProgressService, times(1)).resetLessonCompletion(1L, 1L);
    }

    @Test
    @DisplayName("GET /v1/progress/me/lessons/{lessonId}/completed - Success (Completed)")
    void hasCompletedLesson_Success_True() throws Exception {
        when(userProgressService.hasCompletedLesson(1L, 1L)).thenReturn(true);

        mockMvc.perform(get("/v1/progress/me/lessons/{lessonId}/completed", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed", is(true)));

        verify(userProgressService, times(1)).hasCompletedLesson(1L, 1L);
    }

    @Test
    @DisplayName("GET /v1/progress/me/lessons/{lessonId}/completed - Success (Not Completed)")
    void hasCompletedLesson_Success_False() throws Exception {
        when(userProgressService.hasCompletedLesson(1L, 2L)).thenReturn(false);

        mockMvc.perform(get("/v1/progress/me/lessons/{lessonId}/completed", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed", is(false)));

        verify(userProgressService, times(1)).hasCompletedLesson(1L, 2L);
    }

    @Test
    @DisplayName("GET /v1/progress/me/completed-count - Success")
    void getCompletedLessonCount_Success() throws Exception {
        when(userProgressService.getCompletedLessonCount(1L)).thenReturn(5L);

        mockMvc.perform(get("/v1/progress/me/completed-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completedLessons", is(5)));

        verify(userProgressService, times(1)).getCompletedLessonCount(1L);
    }

    @Test
    @DisplayName("GET /v1/progress/lessons/{lessonId} - Success (Admin)")
    void getLessonProgress_Success() throws Exception {
        when(userProgressService.getLessonProgress(1L))
                .thenReturn(Arrays.asList(progress1));

        mockMvc.perform(get("/v1/progress/lessons/{lessonId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].lessonId", is(1)));

        verify(userProgressService, times(1)).getLessonProgress(1L);
    }

    @Test
    @DisplayName("GET /v1/progress/lessons/{lessonId}/statistics - Success (Admin)")
    void getLessonStatistics_Success() throws Exception {
        Map<String, Object> stats = new HashMap<>();
        stats.put("completedCount", 25L);
        stats.put("averageScore", 85.5);

        when(userProgressService.getLessonStatistics(1L)).thenReturn(stats);

        mockMvc.perform(get("/v1/progress/lessons/{lessonId}/statistics", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completedCount", is(25)))
                .andExpect(jsonPath("$.averageScore", is(85.5)));

        verify(userProgressService, times(1)).getLessonStatistics(1L);
    }
}
