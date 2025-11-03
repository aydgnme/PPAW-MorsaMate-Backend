package me.aydgn.MorseMate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.aydgn.MorseMate.config.SecurityConfig;
import me.aydgn.MorseMate.dto.request.UserSubscriptionRequest;
import me.aydgn.MorseMate.dto.response.SubscriptionPlanResponse;
import me.aydgn.MorseMate.dto.response.UserSubscriptionResponse;
import me.aydgn.MorseMate.exception.GlobalExceptionHandler;
import me.aydgn.MorseMate.exception.ResourceNotFoundException;
import me.aydgn.MorseMate.service.UserSubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for UserSubscriptionController.
 * Tests all user subscription endpoints with various scenarios.
 */
@WebMvcTest(controllers = UserSubscriptionController.class,
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
@DisplayName("UserSubscriptionController Integration Tests")
class UserSubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserSubscriptionService subscriptionService;

    private UserSubscriptionResponse testSubscription;
    private UserSubscriptionRequest testRequest;
    private SubscriptionPlanResponse testPlan;

    @BeforeEach
    void setUp() {
        // Mock plan
        testPlan = SubscriptionPlanResponse.builder()
                .id(1L)
                .name("Premium Monthly")
                .price(new BigDecimal("9.99"))
                .maxHearts(10)
                .isActive(true)
                .build();

        // Mock subscription
        testSubscription = UserSubscriptionResponse.builder()
                .id(1L)
                .userId(100L)
                .username("testuser")
                .plan(testPlan)
                .status("ACTIVE")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusMonths(1))
                .nextBillingDate(LocalDateTime.now().plusMonths(1))
                .autoRenew(true)
                .isActive(true)
                .build();

        // Mock request
        testRequest = UserSubscriptionRequest.builder()
                .planId(1L)
                .autoRenew(true)
                .paymentMethodId("pm_test_123")
                .build();
    }

    // ========== GET /v1/subscriptions/my ==========

    @Test
    @WithMockUser(username = "100")
    @DisplayName("GET /v1/subscriptions/my - Should return current user's subscription")
    void getMySubscription_Success() throws Exception {
        when(subscriptionService.getUserSubscription(100L)).thenReturn(testSubscription);

        mockMvc.perform(get("/v1/subscriptions/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(100))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.plan.name").value("Premium Monthly"))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(subscriptionService).getUserSubscription(100L);
    }

    @Test
    @WithMockUser(username = "100")
    @DisplayName("GET /v1/subscriptions/my - Should return null when no subscription")
    void getMySubscription_NotFound() throws Exception {
        when(subscriptionService.getUserSubscription(100L)).thenReturn(null);

        mockMvc.perform(get("/v1/subscriptions/my"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(subscriptionService).getUserSubscription(100L);
    }

    // ========== POST /v1/subscriptions/subscribe ==========

    @Test
    @WithMockUser(username = "100")
    @DisplayName("POST /v1/subscriptions/subscribe - Should create subscription")
    void subscribe_Success() throws Exception {
        when(subscriptionService.subscribeUser(eq(100L), any(UserSubscriptionRequest.class)))
                .thenReturn(testSubscription);

        mockMvc.perform(post("/v1/subscriptions/subscribe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(100))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.plan.id").value(1));

        verify(subscriptionService).subscribeUser(eq(100L), any(UserSubscriptionRequest.class));
    }

    @Test
    @WithMockUser(username = "100")
    @DisplayName("POST /v1/subscriptions/subscribe - Should fail with null plan ID")
    void subscribe_NullPlanId() throws Exception {
        testRequest.setPlanId(null);

        mockMvc.perform(post("/v1/subscriptions/subscribe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isBadRequest());

        verify(subscriptionService, never()).subscribeUser(any(), any());
    }

    @Test
    @WithMockUser(username = "100")
    @DisplayName("POST /v1/subscriptions/subscribe - Should fail when already subscribed")
    void subscribe_AlreadySubscribed() throws Exception {
        when(subscriptionService.subscribeUser(eq(100L), any(UserSubscriptionRequest.class)))
                .thenThrow(new IllegalStateException("User already has an active subscription"));

        mockMvc.perform(post("/v1/subscriptions/subscribe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User already has an active subscription"));

        verify(subscriptionService).subscribeUser(eq(100L), any(UserSubscriptionRequest.class));
    }

    @Test
    @WithMockUser(username = "100")
    @DisplayName("POST /v1/subscriptions/subscribe - Should fail with invalid plan ID")
    void subscribe_InvalidPlanId() throws Exception {
        when(subscriptionService.subscribeUser(eq(100L), any(UserSubscriptionRequest.class)))
                .thenThrow(new ResourceNotFoundException("Subscription plan not found with ID: 999"));

        mockMvc.perform(post("/v1/subscriptions/subscribe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Subscription plan not found with ID: 999"));

        verify(subscriptionService).subscribeUser(eq(100L), any(UserSubscriptionRequest.class));
    }

    // ========== DELETE /v1/subscriptions/cancel ==========

    @Test
    @WithMockUser(username = "100")
    @DisplayName("DELETE /v1/subscriptions/cancel - Should cancel subscription")
    void cancelSubscription_Success() throws Exception {
        doNothing().when(subscriptionService).cancelSubscription(100L);

        mockMvc.perform(delete("/v1/subscriptions/cancel"))
                .andExpect(status().isOk())
                
                .andExpect(jsonPath("$.message").value("Subscription cancelled successfully"));

        verify(subscriptionService).cancelSubscription(100L);
    }

    @Test
    @WithMockUser(username = "100")
    @DisplayName("DELETE /v1/subscriptions/cancel - Should fail when no subscription")
    void cancelSubscription_NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("No subscription found for user ID: 100"))
                .when(subscriptionService).cancelSubscription(100L);

        mockMvc.perform(delete("/v1/subscriptions/cancel"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No subscription found for user ID: 100"));

        verify(subscriptionService).cancelSubscription(100L);
    }

    @Test
    @WithMockUser(username = "100")
    @DisplayName("DELETE /v1/subscriptions/cancel - Should fail when already cancelled")
    void cancelSubscription_AlreadyCancelled() throws Exception {
        doThrow(new IllegalStateException("Cannot cancel subscription with status: CANCELLED"))
                .when(subscriptionService).cancelSubscription(100L);

        mockMvc.perform(delete("/v1/subscriptions/cancel"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot cancel subscription with status: CANCELLED"));

        verify(subscriptionService).cancelSubscription(100L);
    }

    // ========== GET /v1/subscriptions/history ==========

    @Test
    @WithMockUser(username = "100")
    @DisplayName("GET /v1/subscriptions/history - Should return subscription history")
    void getSubscriptionHistory_Success() throws Exception {
        when(subscriptionService.getSubscriptionHistory(100L))
                .thenReturn(List.of(testSubscription));

        mockMvc.perform(get("/v1/subscriptions/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        verify(subscriptionService).getSubscriptionHistory(100L);
    }

    @Test
    @WithMockUser(username = "100")
    @DisplayName("GET /v1/subscriptions/history - Should return empty list")
    void getSubscriptionHistory_Empty() throws Exception {
        when(subscriptionService.getSubscriptionHistory(100L)).thenReturn(List.of());

        mockMvc.perform(get("/v1/subscriptions/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(subscriptionService).getSubscriptionHistory(100L);
    }

    // ========== PUT /v1/subscriptions/upgrade ==========

    @Test
    @WithMockUser(username = "100")
    @DisplayName("PUT /v1/subscriptions/upgrade - Should upgrade subscription")
    void upgradeSubscription_Success() throws Exception {
        UserSubscriptionResponse upgraded = UserSubscriptionResponse.builder()
                .id(1L)
                .userId(100L)
                .plan(SubscriptionPlanResponse.builder()
                        .id(2L)
                        .name("Premium Yearly")
                        .price(new BigDecimal("99.99"))
                        .build())
                .status("ACTIVE")
                .isActive(true)
                .build();

        when(subscriptionService.upgradeSubscription(100L, 2L)).thenReturn(upgraded);

        mockMvc.perform(put("/v1/subscriptions/upgrade")
                        .param("newPlanId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.plan.id").value(2))
                .andExpect(jsonPath("$.plan.name").value("Premium Yearly"));

        verify(subscriptionService).upgradeSubscription(100L, 2L);
    }

    @Test
    @WithMockUser(username = "100")
    @DisplayName("PUT /v1/subscriptions/upgrade - Should fail on downgrade")
    void upgradeSubscription_DowngradeNotAllowed() throws Exception {
        when(subscriptionService.upgradeSubscription(100L, 2L))
                .thenThrow(new IllegalStateException("Downgrade not supported"));

        mockMvc.perform(put("/v1/subscriptions/upgrade")
                        .param("newPlanId", "2"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Downgrade not supported"));

        verify(subscriptionService).upgradeSubscription(100L, 2L);
    }

    // ========== POST /v1/subscriptions/renew/{id} ==========

    @Test
    @WithMockUser(username = "100")
    @DisplayName("POST /v1/subscriptions/renew/{id} - Should renew subscription")
    void renewSubscription_Success() throws Exception {
        when(subscriptionService.renewSubscription(1L)).thenReturn(testSubscription);

        mockMvc.perform(post("/v1/subscriptions/renew/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(subscriptionService).renewSubscription(1L);
    }

    @Test
    @WithMockUser(username = "100")
    @DisplayName("POST /v1/subscriptions/renew/{id} - Should fail with not found")
    void renewSubscription_NotFound() throws Exception {
        when(subscriptionService.renewSubscription(999L))
                .thenThrow(new ResourceNotFoundException("Subscription not found with ID: 999"));

        mockMvc.perform(post("/v1/subscriptions/renew/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Subscription not found with ID: 999"));

        verify(subscriptionService).renewSubscription(999L);
    }

    // ========== GET /v1/subscriptions/premium-status ==========

    @Test
    @WithMockUser(username = "100")
    @DisplayName("GET /v1/subscriptions/premium-status - Should return true for premium user")
    void checkPremiumStatus_IsPremium() throws Exception {
        when(subscriptionService.isUserPremium(100L)).thenReturn(true);

        mockMvc.perform(get("/v1/subscriptions/premium-status"))
                .andExpect(status().isOk())
                
                .andExpect(jsonPath("$.message").value("User has active premium subscription"));

        verify(subscriptionService).isUserPremium(100L);
    }

    @Test
    @WithMockUser(username = "100")
    @DisplayName("GET /v1/subscriptions/premium-status - Should return false for non-premium user")
    void checkPremiumStatus_NotPremium() throws Exception {
        when(subscriptionService.isUserPremium(100L)).thenReturn(false);

        mockMvc.perform(get("/v1/subscriptions/premium-status"))
                .andExpect(status().isOk())
                
                .andExpect(jsonPath("$.message").value("User does not have premium subscription"));

        verify(subscriptionService).isUserPremium(100L);
    }

    // ========== ADMIN ENDPOINTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /v1/subscriptions/users/{userId} - Admin should get user subscription")
    void getUserSubscription_Admin_Success() throws Exception {
        when(subscriptionService.getUserSubscription(200L)).thenReturn(testSubscription);

        mockMvc.perform(get("/v1/subscriptions/users/200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(100));

        verify(subscriptionService).getUserSubscription(200L);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /v1/subscriptions/users/{userId} - Non-admin should be forbidden")
    void getUserSubscription_NonAdmin_Forbidden() throws Exception {
        mockMvc.perform(get("/v1/subscriptions/users/200"))
                .andExpect(status().isForbidden());

        verify(subscriptionService, never()).getUserSubscription(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /v1/subscriptions/users/{userId}/cancel - Admin should cancel user subscription")
    void cancelUserSubscription_Admin_Success() throws Exception {
        doNothing().when(subscriptionService).cancelSubscription(200L);

        mockMvc.perform(delete("/v1/subscriptions/users/200/cancel"))
                .andExpect(status().isOk())
                
                .andExpect(jsonPath("$.message").value("User subscription cancelled successfully"));

        verify(subscriptionService).cancelSubscription(200L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /v1/subscriptions/users/{userId}/subscribe - Admin should subscribe user")
    void subscribeUser_Admin_Success() throws Exception {
        when(subscriptionService.subscribeUser(eq(200L), any(UserSubscriptionRequest.class)))
                .thenReturn(testSubscription);

        mockMvc.perform(post("/v1/subscriptions/users/200/subscribe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(subscriptionService).subscribeUser(eq(200L), any(UserSubscriptionRequest.class));
    }
}
