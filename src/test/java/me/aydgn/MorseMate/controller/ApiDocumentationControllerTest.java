package me.aydgn.MorseMate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.aydgn.MorseMate.config.JpaConfig;
import me.aydgn.MorseMate.config.JwtAuthenticationFilter;
import me.aydgn.MorseMate.config.SecurityConfig;
import me.aydgn.MorseMate.dto.response.ApiDocumentation;
import me.aydgn.MorseMate.exception.GlobalExceptionHandler;
import me.aydgn.MorseMate.service.ApiDocumentationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ApiDocumentationController.class,
        excludeAutoConfiguration = {
                HibernateJpaAutoConfiguration.class,
                DataSourceAutoConfiguration.class,
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class
        },
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {SecurityConfig.class, JwtAuthenticationFilter.class, JpaConfig.class}
        ))
@Import(GlobalExceptionHandler.class)
@DisplayName("ApiDocumentationController Tests")
class ApiDocumentationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ApiDocumentationService apiDocumentationService;

    @Test
    @DisplayName("Should return API documentation JSON")
    void shouldReturnApiDocumentationJson() throws Exception {
        // Given
        ApiDocumentation.Endpoint endpoint = ApiDocumentation.Endpoint.builder()
                .method("GET")
                .path("/api/health")
                .summary("Health Check")
                .description("Check if the API is running")
                .requiresAuth(false)
                .parameters(Collections.emptyList())
                .build();

        ApiDocumentation.EndpointGroup group = ApiDocumentation.EndpointGroup.builder()
                .name("System")
                .description("System endpoints")
                .endpoints(List.of(endpoint))
                .build();

        ApiDocumentation documentation = ApiDocumentation.builder()
                .title("MorseMate API")
                .version("1.0.0")
                .description("API for learning Morse code")
                .baseUrl("")
                .endpointGroups(List.of(group))
                .build();

        when(apiDocumentationService.getApiDocumentation()).thenReturn(documentation);

        // When & Then
        mockMvc.perform(get("/api-docs")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title", is("MorseMate API")))
                .andExpect(jsonPath("$.version", is("1.0.0")))
                .andExpect(jsonPath("$.description", is("API for learning Morse code")))
                .andExpect(jsonPath("$.endpointGroups", hasSize(1)))
                .andExpect(jsonPath("$.endpointGroups[0].name", is("System")))
                .andExpect(jsonPath("$.endpointGroups[0].endpoints", hasSize(1)))
                .andExpect(jsonPath("$.endpointGroups[0].endpoints[0].method", is("GET")))
                .andExpect(jsonPath("$.endpointGroups[0].endpoints[0].path", is("/api/health")));
    }

    @Test
    @DisplayName("Should serve API documentation UI HTML")
    void shouldServeApiDocumentationUI() throws Exception {
        // When & Then
        mockMvc.perform(get("/api-docs")
                        .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(containsString("MorseMate API Documentation")))
                .andExpect(content().string(containsString("<title>MorseMate API Documentation</title>")));
    }
}
