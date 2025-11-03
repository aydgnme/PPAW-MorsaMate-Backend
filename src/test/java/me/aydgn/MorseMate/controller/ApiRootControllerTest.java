package me.aydgn.MorseMate.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ApiRootController.class,
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
@DisplayName("ApiRootController Tests")
class ApiRootControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET / - Should return API root with available endpoints")
    void root_ReturnsApiRoot() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.message").value("Welcome to MorseMate API"))
                .andExpect(jsonPath("$.version").exists())
                .andExpect(jsonPath("$.endpoints").exists())
                .andExpect(jsonPath("$.endpoints.health").value("/api/health"))
                .andExpect(jsonPath("$.endpoints.ping").value("/api/ping"))
                .andExpect(jsonPath("$.endpoints.info").value("/api/info"))
                .andExpect(jsonPath("$.endpoints.auth").value("/auth/*"))
                .andExpect(jsonPath("$.endpoints.categories").value("/v1/categories"));
    }

    @Test
    @DisplayName("GET /api - Should return same as root endpoint")
    void apiRoot_ReturnsApiRoot() throws Exception {
        mockMvc.perform(get("/api"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.message").value("Welcome to MorseMate API"))
                .andExpect(jsonPath("$.version").exists())
                .andExpect(jsonPath("$.endpoints").exists());
    }
}
