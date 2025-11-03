package me.aydgn.MorseMate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.aydgn.MorseMate.dto.request.SubscriptionPlanRequest;
import me.aydgn.MorseMate.dto.response.SubscriptionPlanResponse;
import me.aydgn.MorseMate.entity.SubscriptionPlan;
import me.aydgn.MorseMate.exception.DuplicateResourceException;
import me.aydgn.MorseMate.exception.GlobalExceptionHandler;
import me.aydgn.MorseMate.exception.ResourceNotFoundException;
import me.aydgn.MorseMate.service.SubscriptionPlanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SubscriptionPlanController.class,
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
@DisplayName("SubscriptionPlanController Integration Tests")
class SubscriptionPlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SubscriptionPlanService subscriptionPlanService;

    private SubscriptionPlanResponse basicPlan;
    private SubscriptionPlanResponse premiumPlan;
    private SubscriptionPlanRequest createRequest;
    private Map<String, Object> features;

    @BeforeEach
    void setUp() {
        features = new HashMap<>();
        features.put("unlimited_hearts", true);
        features.put("ad_free", true);
        features.put("offline_mode", true);

        basicPlan = SubscriptionPlanResponse.builder()
                .id(1L)
                .name("Basic Plan")
                .description("Basic subscription with limited features")
                .price(new BigDecimal("9.99"))
                .billingPeriod("MONTHLY")
                .features(features)
                .maxHearts(5)
                .dailyPracticeLimit(50)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        premiumPlan = SubscriptionPlanResponse.builder()
                .id(2L)
                .name("Premium Plan")
                .description("Premium subscription with all features")
                .price(new BigDecimal("99.99"))
                .billingPeriod("YEARLY")
                .features(features)
                .maxHearts(100)
                .dailyPracticeLimit(null)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        createRequest = SubscriptionPlanRequest.builder()
                .name("Pro Plan")
                .description("Professional subscription")
                .price(new BigDecimal("19.99"))
                .billingPeriod(SubscriptionPlan.BillingPeriod.MONTHLY)
                .features(features)
                .maxHearts(10)
                .dailyPracticeLimit(100)
                .isActive(true)
                .build();
    }

    // ================ GET /v1/subscriptions/plans Tests ================

    @Test
    @DisplayName("GET /v1/subscriptions/plans - Should return all active plans")
    void getAllActivePlans_Success() throws Exception {
        // Given
        List<SubscriptionPlanResponse> plans = Arrays.asList(basicPlan, premiumPlan);
        when(subscriptionPlanService.getAllActivePlans()).thenReturn(plans);

        // When & Then
        mockMvc.perform(get("/v1/subscriptions/plans"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Basic Plan"))
                .andExpect(jsonPath("$[0].price").value(9.99))
                .andExpect(jsonPath("$[1].name").value("Premium Plan"));

        verify(subscriptionPlanService, times(1)).getAllActivePlans();
    }

    @Test
    @DisplayName("GET /v1/subscriptions/plans - Should return empty list when no plans")
    void getAllActivePlans_EmptyList() throws Exception {
        // Given
        when(subscriptionPlanService.getAllActivePlans()).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/v1/subscriptions/plans"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(subscriptionPlanService, times(1)).getAllActivePlans();
    }

    // ================ GET /v1/subscriptions/plans/{id} Tests ================

    @Test
    @DisplayName("GET /v1/subscriptions/plans/{id} - Should return plan by ID")
    void getPlanById_Success() throws Exception {
        // Given
        when(subscriptionPlanService.getPlanById(1L)).thenReturn(basicPlan);

        // When & Then
        mockMvc.perform(get("/v1/subscriptions/plans/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Basic Plan"))
                .andExpect(jsonPath("$.price").value(9.99))
                .andExpect(jsonPath("$.billingPeriod").value("MONTHLY"))
                .andExpect(jsonPath("$.maxHearts").value(5));

        verify(subscriptionPlanService, times(1)).getPlanById(1L);
    }

    @Test
    @DisplayName("GET /v1/subscriptions/plans/{id} - Should return 404 when not found")
    void getPlanById_NotFound() throws Exception {
        // Given
        when(subscriptionPlanService.getPlanById(999L))
                .thenThrow(new ResourceNotFoundException("SubscriptionPlan", "id", 999L));

        // When & Then
        mockMvc.perform(get("/v1/subscriptions/plans/999"))
                .andExpect(status().isNotFound());

        verify(subscriptionPlanService, times(1)).getPlanById(999L);
    }

    // ================ GET /v1/subscriptions/plans/period/{period} Tests ================

    @Test
    @DisplayName("GET /v1/subscriptions/plans/period/MONTHLY - Should return monthly plans")
    void getPlansByBillingPeriod_Success() throws Exception {
        // Given
        List<SubscriptionPlanResponse> monthlyPlans = Arrays.asList(basicPlan);
        when(subscriptionPlanService.getPlansByBillingPeriod(SubscriptionPlan.BillingPeriod.MONTHLY))
                .thenReturn(monthlyPlans);

        // When & Then
        mockMvc.perform(get("/v1/subscriptions/plans/period/MONTHLY"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].billingPeriod").value("MONTHLY"));

        verify(subscriptionPlanService, times(1))
                .getPlansByBillingPeriod(SubscriptionPlan.BillingPeriod.MONTHLY);
    }

    // ================ POST /v1/subscriptions/plans Tests ================

    @Test
    @DisplayName("POST /v1/subscriptions/plans - Should create new plan")
    void createPlan_Success() throws Exception {
        // Given
        SubscriptionPlanResponse created = SubscriptionPlanResponse.builder()
                .id(3L)
                .name(createRequest.getName())
                .description(createRequest.getDescription())
                .price(createRequest.getPrice())
                .billingPeriod("MONTHLY")
                .features(createRequest.getFeatures())
                .maxHearts(createRequest.getMaxHearts())
                .dailyPracticeLimit(createRequest.getDailyPracticeLimit())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(subscriptionPlanService.createPlan(any(SubscriptionPlanRequest.class))).thenReturn(created);

        // When & Then
        mockMvc.perform(post("/v1/subscriptions/plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("Pro Plan"))
                .andExpect(jsonPath("$.price").value(19.99));

        verify(subscriptionPlanService, times(1)).createPlan(any(SubscriptionPlanRequest.class));
    }

    @Test
    @DisplayName("POST /v1/subscriptions/plans - Should return 400 with invalid data")
    void createPlan_InvalidData() throws Exception {
        // Given - Invalid request (missing name)
        SubscriptionPlanRequest invalidRequest = SubscriptionPlanRequest.builder()
                .description("Test")
                .price(new BigDecimal("9.99"))
                .billingPeriod(SubscriptionPlan.BillingPeriod.MONTHLY)
                .maxHearts(5)
                .build();

        // When & Then
        mockMvc.perform(post("/v1/subscriptions/plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(subscriptionPlanService, never()).createPlan(any(SubscriptionPlanRequest.class));
    }

    @Test
    @DisplayName("POST /v1/subscriptions/plans - Should return 409 when duplicate name")
    void createPlan_DuplicateName() throws Exception {
        // Given
        when(subscriptionPlanService.createPlan(any(SubscriptionPlanRequest.class)))
                .thenThrow(new DuplicateResourceException("SubscriptionPlan", "name", createRequest.getName()));

        // When & Then
        mockMvc.perform(post("/v1/subscriptions/plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isConflict());

        verify(subscriptionPlanService, times(1)).createPlan(any(SubscriptionPlanRequest.class));
    }

    // ================ PUT /v1/subscriptions/plans/{id} Tests ================

    @Test
    @DisplayName("PUT /v1/subscriptions/plans/{id} - Should update plan")
    void updatePlan_Success() throws Exception {
        // Given
        SubscriptionPlanResponse updated = SubscriptionPlanResponse.builder()
                .id(1L)
                .name("Updated Basic Plan")
                .description("Updated description")
                .price(new BigDecimal("12.99"))
                .billingPeriod("MONTHLY")
                .features(features)
                .maxHearts(10)
                .isActive(true)
                .updatedAt(LocalDateTime.now())
                .build();

        when(subscriptionPlanService.updatePlan(eq(1L), any(SubscriptionPlanRequest.class)))
                .thenReturn(updated);

        // When & Then
        mockMvc.perform(put("/v1/subscriptions/plans/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Basic Plan"));

        verify(subscriptionPlanService, times(1))
                .updatePlan(eq(1L), any(SubscriptionPlanRequest.class));
    }

    @Test
    @DisplayName("PUT /v1/subscriptions/plans/{id} - Should return 404 when not found")
    void updatePlan_NotFound() throws Exception {
        // Given
        when(subscriptionPlanService.updatePlan(eq(999L), any(SubscriptionPlanRequest.class)))
                .thenThrow(new ResourceNotFoundException("SubscriptionPlan", "id", 999L));

        // When & Then
        mockMvc.perform(put("/v1/subscriptions/plans/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isNotFound());

        verify(subscriptionPlanService, times(1))
                .updatePlan(eq(999L), any(SubscriptionPlanRequest.class));
    }

    // ================ DELETE /v1/subscriptions/plans/{id} Tests ================

    @Test
    @DisplayName("DELETE /v1/subscriptions/plans/{id} - Should delete plan")
    void deletePlan_Success() throws Exception {
        // Given
        doNothing().when(subscriptionPlanService).deletePlan(1L);

        // When & Then
        mockMvc.perform(delete("/v1/subscriptions/plans/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Subscription plan deleted successfully"));

        verify(subscriptionPlanService, times(1)).deletePlan(1L);
    }

    @Test
    @DisplayName("DELETE /v1/subscriptions/plans/{id} - Should return 404 when not found")
    void deletePlan_NotFound() throws Exception {
        // Given
        doThrow(new ResourceNotFoundException("SubscriptionPlan", "id", 999L))
                .when(subscriptionPlanService).deletePlan(999L);

        // When & Then
        mockMvc.perform(delete("/v1/subscriptions/plans/999"))
                .andExpect(status().isNotFound());

        verify(subscriptionPlanService, times(1)).deletePlan(999L);
    }

    // ================ GET /v1/subscriptions/plans/admin/all Tests ================

    @Test
    @DisplayName("GET /v1/subscriptions/plans/admin/all - Should return all plans")
    void getAllPlans_Success() throws Exception {
        // Given
        List<SubscriptionPlanResponse> allPlans = Arrays.asList(basicPlan, premiumPlan);
        when(subscriptionPlanService.getAllPlans()).thenReturn(allPlans);

        // When & Then
        mockMvc.perform(get("/v1/subscriptions/plans/admin/all"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)));

        verify(subscriptionPlanService, times(1)).getAllPlans();
    }
}
