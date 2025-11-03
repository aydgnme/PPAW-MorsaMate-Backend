package me.aydgn.MorseMate.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

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
        ),
        properties = {
                "app.version=1.0.0-TEST",
                "app.name=MorseMate",
                "app.redirect-url=https://aydgn.me"
        })
@Import(TestSecurityConfig.class)
@DisplayName("ApiRootController Tests")
class ApiRootControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET / - Should redirect to aydgn.me when not authenticated")
    void root_RedirectsWhenNotAuthenticated() throws Exception {
        // Without authentication, should redirect
        mockMvc.perform(get("/"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://aydgn.me"));
    }

    @Test
    @DisplayName("GET /api - Should redirect to aydgn.me when not authenticated")
    void apiRoot_RedirectsWhenNotAuthenticated() throws Exception {
        // Without authentication, should redirect
        mockMvc.perform(get("/api"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://aydgn.me"));
    }
}
