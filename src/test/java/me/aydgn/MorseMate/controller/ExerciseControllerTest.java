package me.aydgn.MorseMate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.aydgn.MorseMate.dto.request.CreateExerciseRequest;
import me.aydgn.MorseMate.dto.request.UpdateExerciseRequest;
import me.aydgn.MorseMate.dto.response.ExerciseResponse;
import me.aydgn.MorseMate.exception.GlobalExceptionHandler;
import me.aydgn.MorseMate.exception.ResourceNotFoundException;
import me.aydgn.MorseMate.service.ExerciseService;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ExerciseController.class, excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
                org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class,
                org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration.class
}, excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE, classes = {
                me.aydgn.MorseMate.config.SecurityConfig.class,
                me.aydgn.MorseMate.config.JwtAuthenticationFilter.class,
                me.aydgn.MorseMate.config.JpaConfig.class
}))
@Import(GlobalExceptionHandler.class)
@WithMockUser(username = "1")
@DisplayName("ExerciseController Integration Tests")
class ExerciseControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private ExerciseService exerciseService;

        private ExerciseResponse exercise1;
        private ExerciseResponse exercise2;
        private CreateExerciseRequest createRequest;
        private UpdateExerciseRequest updateRequest;

        @BeforeEach
        void setUp() {
                exercise1 = ExerciseResponse.builder()
                                .id(1L)
                                .lessonId(1L)
                                .lessonTitle("Introduction to Morse Code")
                                .type("ENCODE")
                                .question("Encode the letter 'A' in Morse code")
                                .correctAnswer(".-")
                                .difficulty("EASY")
                                .points(5)
                                .timeLimit(30)
                                .hint("A is represented by a dot followed by a dash")
                                .createdAt(LocalDateTime.now())
                                .build();

                exercise2 = ExerciseResponse.builder()
                                .id(2L)
                                .lessonId(1L)
                                .lessonTitle("Introduction to Morse Code")
                                .type("DECODE")
                                .question("Decode: -...")
                                .correctAnswer("B")
                                .difficulty("EASY")
                                .points(5)
                                .timeLimit(30)
                                .createdAt(LocalDateTime.now())
                                .build();

                createRequest = CreateExerciseRequest.builder()
                                .lessonId(1L)
                                .type("ENCODE")
                                .question("Encode the letter 'C' in Morse code")
                                .correctAnswer("-.-.")
                                .difficulty("EASY")
                                .points(5)
                                .timeLimit(30)
                                .hint("C is dash-dot-dash-dot")
                                .build();

                updateRequest = UpdateExerciseRequest.builder()
                                .id(1L)
                                .question("Updated question")
                                .correctAnswer("Updated answer")
                                .points(10)
                                .build();
        }

        @Test
        @DisplayName("GET /v1/exercises/{id} - Success")
        void getExerciseById_Success() throws Exception {
                when(exerciseService.getExerciseById(1L)).thenReturn(exercise1);

                mockMvc.perform(get("/v1/exercises/{id}", 1L))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id", is(1)))
                                .andExpect(jsonPath("$.lessonId", is(1)))
                                .andExpect(jsonPath("$.type", is("ENCODE")))
                                .andExpect(jsonPath("$.question", is("Encode the letter 'A' in Morse code")))
                                .andExpect(jsonPath("$.difficulty", is("EASY")));

                verify(exerciseService, times(1)).getExerciseById(1L);
        }

        @Test
        @DisplayName("GET /v1/exercises/{id} - Not Found")
        void getExerciseById_NotFound() throws Exception {
                when(exerciseService.getExerciseById(999L))
                                .thenThrow(new ResourceNotFoundException("Exercise", "id", 999L));

                mockMvc.perform(get("/v1/exercises/{id}", 999L))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.message", containsString("Exercise not found")));
        }

        @Test
        @DisplayName("GET /v1/exercises?lessonId={id} - Success")
        void getExercisesByLessonId_Success() throws Exception {
                List<ExerciseResponse> exercises = Arrays.asList(exercise1, exercise2);
                when(exerciseService.getExercisesByLessonId(1L)).thenReturn(exercises);

                mockMvc.perform(get("/v1/exercises")
                                .param("lessonId", "1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(2)))
                                .andExpect(jsonPath("$[0].id", is(1)))
                                .andExpect(jsonPath("$[1].id", is(2)));

                verify(exerciseService, times(1)).getExercisesByLessonId(1L);
        }

        @Test
        @DisplayName("GET /v1/exercises?lessonId={id}&type={type} - Success")
        void getExercisesByLessonIdAndType_Success() throws Exception {
                when(exerciseService.getExercisesByLessonAndType(1L, "ENCODE"))
                                .thenReturn(Arrays.asList(exercise1));

                mockMvc.perform(get("/v1/exercises")
                                .param("lessonId", "1")
                                .param("type", "ENCODE"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(1)))
                                .andExpect(jsonPath("$[0].type", is("ENCODE")));

                verify(exerciseService, times(1)).getExercisesByLessonAndType(1L, "ENCODE");
        }

        @Test
        @DisplayName("GET /v1/exercises/random - Success")
        void getRandomExercise_Success() throws Exception {
                when(exerciseService.getRandomExercise(eq(1L), isNull(), isNull()))
                                .thenReturn(exercise1);

                mockMvc.perform(get("/v1/exercises/random")
                                .param("lessonId", "1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id", is(1)))
                                .andExpect(jsonPath("$.type", is("ENCODE")));

                verify(exerciseService, times(1)).getRandomExercise(eq(1L), isNull(), isNull());
        }

        @Test
        @DisplayName("GET /v1/exercises/search - Success")
        void searchExercises_Success() throws Exception {
                Page<ExerciseResponse> page = new PageImpl<>(Arrays.asList(exercise1));
                when(exerciseService.searchExercisesByQuestion(anyString(), any()))
                                .thenReturn(page);

                mockMvc.perform(get("/v1/exercises/search")
                                .param("question", "encode"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content", hasSize(1)))
                                .andExpect(jsonPath("$.content[0].question", containsString("Encode")));
        }

        @Test
        @DisplayName("POST /v1/exercises - Success")
        void createExercise_Success() throws Exception {
                when(exerciseService.createExercise(any(CreateExerciseRequest.class)))
                                .thenReturn(exercise1);

                mockMvc.perform(post("/v1/exercises")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id", is(1)))
                                .andExpect(jsonPath("$.type", is("ENCODE")));

                verify(exerciseService, times(1)).createExercise(any(CreateExerciseRequest.class));
        }

        @Test
        @DisplayName("POST /v1/exercises - Validation Error")
        void createExercise_ValidationError() throws Exception {
                CreateExerciseRequest invalidRequest = CreateExerciseRequest.builder().build();

                mockMvc.perform(post("/v1/exercises")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("PUT /v1/exercises/{id} - Success")
        void updateExercise_Success() throws Exception {
                ExerciseResponse updated = ExerciseResponse.builder()
                                .id(1L)
                                .lessonId(1L)
                                .lessonTitle("Introduction to Morse Code")
                                .type("ENCODE")
                                .question("Updated question")
                                .correctAnswer(".-")
                                .difficulty("EASY")
                                .points(10)
                                .timeLimit(30)
                                .hint("A is represented by a dot followed by a dash")
                                .createdAt(LocalDateTime.now())
                                .build();
                when(exerciseService.updateExercise(eq(1L), any(UpdateExerciseRequest.class)))
                                .thenReturn(updated);

                mockMvc.perform(put("/v1/exercises/{id}", 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.question").value("Updated question"))
                                .andExpect(jsonPath("$.points").value(10));

                verify(exerciseService, times(1)).updateExercise(eq(1L), any(UpdateExerciseRequest.class));
        }

        @Test
        @DisplayName("DELETE /v1/exercises/{id} - Success")
        void deleteExercise_Success() throws Exception {
                doNothing().when(exerciseService).deleteExercise(1L);

                mockMvc.perform(delete("/v1/exercises/{id}", 1L))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message", is("Exercise deleted successfully")));

                verify(exerciseService, times(1)).deleteExercise(1L);
        }

        @Test
        @DisplayName("GET /v1/exercises/count - Success")
        void getExerciseCount_Success() throws Exception {
                when(exerciseService.getExerciseCountByLesson(1L)).thenReturn(10L);

                mockMvc.perform(get("/v1/exercises/count")
                                .param("lessonId", "1"))
                                .andExpect(status().isOk())
                                .andExpect(content().string("10"));

                verify(exerciseService, times(1)).getExerciseCountByLesson(1L);
        }
}
