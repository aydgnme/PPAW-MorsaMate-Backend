package me.aydgn.MorseMate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.aydgn.MorseMate.dto.request.CreateExerciseAttemptRequest;
import me.aydgn.MorseMate.dto.response.ExerciseAttemptResponse;
import me.aydgn.MorseMate.exception.GlobalExceptionHandler;
import me.aydgn.MorseMate.exception.ResourceNotFoundException;
import me.aydgn.MorseMate.service.ExerciseAttemptService;
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
import org.springframework.security.test.context.support.WithMockUser;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ExerciseAttemptController.class, excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
                org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
                org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration.class
}, excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE, classes = {
                me.aydgn.MorseMate.config.SecurityConfig.class,
                me.aydgn.MorseMate.config.JwtAuthenticationFilter.class,
                me.aydgn.MorseMate.config.JpaConfig.class
}))
@Import({ GlobalExceptionHandler.class, TestSecurityConfig.class })
@WithMockUser(username = "1")
@DisplayName("ExerciseAttemptController Integration Tests")
class ExerciseAttemptControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private ExerciseAttemptService exerciseAttemptService;

        private ExerciseAttemptResponse attempt1;
        private ExerciseAttemptResponse attempt2;
        private CreateExerciseAttemptRequest createRequest;

        @BeforeEach
        void setUp() {
                attempt1 = ExerciseAttemptResponse.builder()
                                .id(1L)
                                .userId(1L)
                                .exerciseId(1L)
                                .userAnswer(".-")
                                .isCorrect(true)
                                .timeTaken(15)
                                .pointsEarned(5)
                                .attemptedAt(LocalDateTime.now())
                                .build();

                attempt2 = ExerciseAttemptResponse.builder()
                                .id(2L)
                                .userId(1L)
                                .exerciseId(2L)
                                .userAnswer("ABC")
                                .isCorrect(false)
                                .timeTaken(25)
                                .pointsEarned(0)
                                .attemptedAt(LocalDateTime.now())
                                .build();

                createRequest = CreateExerciseAttemptRequest.builder()
                                .userId(1L)
                                .exerciseId(1L)
                                .userAnswer(".-")
                                .timeTaken(15)
                                .build();
        }

        @Test
        @DisplayName("POST /v1/attempts - Success")
        void recordAttempt_Success() throws Exception {
                when(exerciseAttemptService.recordAttempt(eq(1L), any(CreateExerciseAttemptRequest.class)))
                                .thenReturn(attempt1);

                mockMvc.perform(post("/v1/attempts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.userId").value(1))
                                .andExpect(jsonPath("$.exerciseId").value(1))
                                .andExpect(jsonPath("$.isCorrect").value(true))
                                .andExpect(jsonPath("$.pointsEarned").value(5));

                verify(exerciseAttemptService, times(1))
                                .recordAttempt(eq(1L), any(CreateExerciseAttemptRequest.class));
        }

        @Test
        @DisplayName("POST /v1/attempts - Validation Error")
        void recordAttempt_ValidationError() throws Exception {
                CreateExerciseAttemptRequest invalidRequest = CreateExerciseAttemptRequest.builder().build();

                mockMvc.perform(post("/v1/attempts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("GET /v1/attempts/me - Success")
        void getMyAttempts_Success() throws Exception {
                List<ExerciseAttemptResponse> attempts = Arrays.asList(attempt1, attempt2);
                when(exerciseAttemptService.getUserAttempts(1L)).thenReturn(attempts);

                mockMvc.perform(get("/v1/attempts/me"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(2)))
                                .andExpect(jsonPath("$[0].id", is(1)))
                                .andExpect(jsonPath("$[1].id", is(2)));

                verify(exerciseAttemptService, times(1)).getUserAttempts(1L);
        }

        @Test
        @DisplayName("GET /v1/attempts/me/paged - Success")
        void getMyAttemptsPaged_Success() throws Exception {
                Page<ExerciseAttemptResponse> page = new PageImpl<>(Arrays.asList(attempt1, attempt2));
                when(exerciseAttemptService.getUserAttemptsPaged(eq(1L), any()))
                                .thenReturn(page);

                mockMvc.perform(get("/v1/attempts/me/paged"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content", hasSize(2)))
                                .andExpect(jsonPath("$.content[0].id", is(1)));

                verify(exerciseAttemptService, times(1)).getUserAttemptsPaged(eq(1L), any());
        }

        @Test
        @DisplayName("GET /v1/attempts/me/exercises/{exerciseId} - Success")
        void getMyExerciseHistory_Success() throws Exception {
                when(exerciseAttemptService.getUserExerciseHistory(1L, 1L))
                                .thenReturn(Arrays.asList(attempt1));

                mockMvc.perform(get("/v1/attempts/me/exercises/{exerciseId}", 1L))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(1)))
                                .andExpect(jsonPath("$[0].exerciseId", is(1)));

                verify(exerciseAttemptService, times(1)).getUserExerciseHistory(1L, 1L);
        }

        @Test
        @DisplayName("GET /v1/attempts/me/exercises/{exerciseId}/last - Success")
        void getMyLastAttempt_Success() throws Exception {
                when(exerciseAttemptService.getLastAttempt(1L, 1L)).thenReturn(attempt1);

                mockMvc.perform(get("/v1/attempts/me/exercises/{exerciseId}/last", 1L))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id", is(1)))
                                .andExpect(jsonPath("$.exerciseId", is(1)));

                verify(exerciseAttemptService, times(1)).getLastAttempt(1L, 1L);
        }

        @Test
        @DisplayName("GET /v1/attempts/me/exercises/{exerciseId}/last - Not Found")
        void getMyLastAttempt_NotFound() throws Exception {
                when(exerciseAttemptService.getLastAttempt(1L, 999L))
                                .thenThrow(new ResourceNotFoundException("ExerciseAttempt", "exerciseId", 999L));

                mockMvc.perform(get("/v1/attempts/me/exercises/{exerciseId}/last", 999L))
                                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("GET /v1/attempts/me/exercises/{exerciseId}/statistics - Success")
        void getMyExerciseStatistics_Success() throws Exception {
                Map<String, Object> stats = new HashMap<>();
                stats.put("totalAttempts", 10L);
                stats.put("correctAttempts", 7L);
                stats.put("successRate", 70.0);
                stats.put("averageTimeSeconds", 18.5);

                when(exerciseAttemptService.getUserExerciseStatistics(1L, 1L)).thenReturn(stats);

                mockMvc.perform(get("/v1/attempts/me/exercises/{exerciseId}/statistics", 1L))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.totalAttempts", is(10)))
                                .andExpect(jsonPath("$.correctAttempts", is(7)))
                                .andExpect(jsonPath("$.successRate", is(70.0)));

                verify(exerciseAttemptService, times(1)).getUserExerciseStatistics(1L, 1L);
        }

        @Test
        @DisplayName("GET /v1/attempts/me/statistics - Success")
        void getMyStatistics_Success() throws Exception {
                Map<String, Object> stats = new HashMap<>();
                stats.put("totalAttempts", 50L);
                stats.put("correctAttempts", 35L);
                stats.put("successRate", 70.0);

                when(exerciseAttemptService.getUserStatistics(1L)).thenReturn(stats);

                mockMvc.perform(get("/v1/attempts/me/statistics"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.totalAttempts", is(50)))
                                .andExpect(jsonPath("$.correctAttempts", is(35)))
                                .andExpect(jsonPath("$.successRate", is(70.0)));

                verify(exerciseAttemptService, times(1)).getUserStatistics(1L);
        }

        @Test
        @DisplayName("GET /v1/attempts/me/range - Success")
        void getMyAttemptsInRange_Success() throws Exception {
                when(exerciseAttemptService.getUserAttemptsInRange(eq(1L), any(), any()))
                                .thenReturn(Arrays.asList(attempt1, attempt2));

                mockMvc.perform(get("/v1/attempts/me/range")

                                .param("from", "2025-01-01T00:00:00")
                                .param("to", "2025-12-31T23:59:59"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(2)));

                verify(exerciseAttemptService, times(1))
                                .getUserAttemptsInRange(eq(1L), any(), any());
        }

        @Test
        @DisplayName("GET /v1/attempts/me/correctness-rate - Success")
        void getCorrectnessRate_Success() throws Exception {
                when(exerciseAttemptService.getCorrectnessRate(eq(1L), any(), any()))
                                .thenReturn(75.5);

                mockMvc.perform(get("/v1/attempts/me/correctness-rate")

                                .param("from", "2025-01-01T00:00:00")
                                .param("to", "2025-12-31T23:59:59"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.correctnessRate", is(75.5)));

                verify(exerciseAttemptService, times(1))
                                .getCorrectnessRate(eq(1L), any(), any());
        }
}
