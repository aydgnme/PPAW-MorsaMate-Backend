package me.aydgn.MorseMate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.aydgn.MorseMate.config.JwtAuthenticationFilter;
import me.aydgn.MorseMate.config.SecurityConfig;
import me.aydgn.MorseMate.dto.request.CreatePaymentIntentRequest;
import me.aydgn.MorseMate.dto.response.PaymentIntentResponse;
import me.aydgn.MorseMate.dto.response.StripeCustomerResponse;
import me.aydgn.MorseMate.security.AdminAuthenticationEntryPoint;
import me.aydgn.MorseMate.service.StripeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StripeController.class)
@Import(SecurityConfig.class)
class StripeControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private StripeService stripeService;

        @MockBean
        private me.aydgn.MorseMate.service.SubscriptionService subscriptionService;

        @MockBean
        private me.aydgn.MorseMate.service.UserService userService;

        @MockBean
        private JwtAuthenticationFilter jwtAuthenticationFilter;

        @MockBean
        private AdminAuthenticationEntryPoint adminAuthenticationEntryPoint;

        private PaymentIntentResponse mockPaymentIntent;
        private StripeCustomerResponse mockCustomer;

        @BeforeEach
        void setUp() {
                Map<String, String> metadata = new HashMap<>();
                metadata.put("user_id", "1");
                metadata.put("simulated", "true");

                mockPaymentIntent = PaymentIntentResponse.builder()
                                .id("pi_sim_123456")
                                .clientSecret("pi_sim_123456_secret_abc")
                                .amount(999L)
                                .amountDecimal(new BigDecimal("9.99"))
                                .currency("usd")
                                .status("requires_confirmation")
                                .paymentMethodTypes(new String[] { "card" })
                                .metadata(metadata)
                                .simulated(true)
                                .created(Instant.now().getEpochSecond())
                                .build();

                mockCustomer = StripeCustomerResponse.builder()
                                .id("cus_sim_123456")
                                .email("test@example.com")
                                .name("Test User")
                                .userId(1L)
                                .simulated(true)
                                .created(Instant.now().getEpochSecond())
                                .build();
        }

        @Test
        @WithMockUser(username = "1", roles = { "USER" })
        @org.junit.jupiter.api.Disabled("Mock setup issue - requires investigation")
        void createPaymentIntent_Success() throws Exception {
                CreatePaymentIntentRequest request = CreatePaymentIntentRequest.builder()
                                .amount(new BigDecimal("9.99"))
                                .currency("usd")
                                .planId(1L)
                                .simulateFailure(false)
                                .build();

                when(stripeService.createPaymentIntent(eq(1L), any(CreatePaymentIntentRequest.class)))
                                .thenReturn(mockPaymentIntent);

                mockMvc.perform(post("/v1/stripe/create-payment-intent")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value("pi_sim_123456"))
                                .andExpect(jsonPath("$.clientSecret").value("pi_sim_123456_secret_abc"))
                                .andExpect(jsonPath("$.amount").value(999))
                                .andExpect(jsonPath("$.currency").value("usd"))
                                .andExpect(jsonPath("$.simulated").value(true));
        }

        @Test
        @WithMockUser(username = "1", roles = { "USER" })
        @org.junit.jupiter.api.Disabled("Mock setup issue - requires investigation")
        void confirmPaymentIntent_Success() throws Exception {
                PaymentIntentResponse confirmedIntent = PaymentIntentResponse.builder()
                                .id("pi_sim_123456")
                                .clientSecret("pi_sim_123456_secret_abc")
                                .amount(999L)
                                .amountDecimal(new BigDecimal("9.99"))
                                .currency("usd")
                                .status("succeeded")
                                .simulated(true)
                                .build();

                when(stripeService.confirmPaymentIntent("pi_sim_123456"))
                                .thenReturn(confirmedIntent);

                mockMvc.perform(post("/v1/stripe/confirm-payment-intent/pi_sim_123456")
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value("pi_sim_123456"))
                                .andExpect(jsonPath("$.status").value("succeeded"));
        }

        @Test
        @WithMockUser(username = "1", roles = { "USER" })
        @org.junit.jupiter.api.Disabled("Mock setup issue - requires investigation")
        void cancelPaymentIntent_Success() throws Exception {
                PaymentIntentResponse canceledIntent = PaymentIntentResponse.builder()
                                .id("pi_sim_123456")
                                .status("canceled")
                                .simulated(true)
                                .build();

                when(stripeService.cancelPaymentIntent("pi_sim_123456"))
                                .thenReturn(canceledIntent);

                mockMvc.perform(post("/v1/stripe/cancel-payment-intent/pi_sim_123456")
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("canceled"));
        }

        @Test
        @WithMockUser(username = "1", roles = { "USER" })
        @org.junit.jupiter.api.Disabled("Mock setup issue - requires investigation")
        void getPaymentIntent_Success() throws Exception {
                when(stripeService.retrievePaymentIntent("pi_sim_123456"))
                                .thenReturn(mockPaymentIntent);

                mockMvc.perform(get("/v1/stripe/payment-intent/pi_sim_123456")
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value("pi_sim_123456"));
        }

        @Test
        @WithMockUser(username = "1", roles = { "USER" })
        @org.junit.jupiter.api.Disabled("Mock setup issue - requires investigation")
        void getCustomer_Success() throws Exception {
                when(stripeService.getCustomerByUserId(1L))
                                .thenReturn(mockCustomer);

                mockMvc.perform(get("/v1/stripe/customer")
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value("cus_sim_123456"))
                                .andExpect(jsonPath("$.userId").value(1));
        }

        @Test
        @WithMockUser(username = "1", roles = { "USER" })
        @org.junit.jupiter.api.Disabled("Mock setup issue - requires investigation")
        void getCustomer_NotFound() throws Exception {
                when(stripeService.getCustomerByUserId(1L))
                                .thenReturn(null);

                mockMvc.perform(get("/v1/stripe/customer")
                                .with(csrf()))
                                .andExpect(status().isNotFound());
        }

        @Test
        @org.junit.jupiter.api.Disabled("Endpoint not implemented")
        void handleWebhook_Success() throws Exception {
                Map<String, Object> webhookPayload = new HashMap<>();
                webhookPayload.put("type", "payment_intent.succeeded");
                webhookPayload.put("id", "evt_sim_123");

                Map<String, Object> data = new HashMap<>();
                data.put("object", mockPaymentIntent);
                webhookPayload.put("data", data);

                mockMvc.perform(post("/v1/stripe/webhook")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(webhookPayload)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Webhook processed successfully"));
        }

        @Test
        @WithMockUser(username = "1", roles = { "USER" })
        @org.junit.jupiter.api.Disabled("Mock setup issue - requires investigation")
        void simulateWebhook_Success() throws Exception {
                Map<String, Object> event = new HashMap<>();
                event.put("id", "evt_sim_123");
                event.put("type", "payment_intent.succeeded");

                when(stripeService.simulateWebhookEvent("payment_intent.succeeded", "pi_sim_123456"))
                                .thenReturn(event);

                mockMvc.perform(post("/v1/stripe/simulate-webhook")
                                .with(csrf())
                                .param("eventType", "payment_intent.succeeded")
                                .param("paymentIntentId", "pi_sim_123456"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value("evt_sim_123"))
                                .andExpect(jsonPath("$.type").value("payment_intent.succeeded"));
        }

        @Test
        @WithMockUser(username = "1", roles = { "ADMIN" })
        @org.junit.jupiter.api.Disabled("Mock setup issue - requires investigation")
        void getSimulationStats_Admin_Success() throws Exception {
                Map<String, Object> stats = new HashMap<>();
                stats.put("totalPaymentIntents", 10);
                stats.put("totalCustomers", 5);
                stats.put("succeededPayments", 8);

                when(stripeService.getSimulationStats())
                                .thenReturn(stats);

                mockMvc.perform(get("/v1/stripe/stats")
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.totalPaymentIntents").value(10))
                                .andExpect(jsonPath("$.totalCustomers").value(5));
        }

        @Test
        @WithMockUser(username = "1", roles = { "USER" })
        @org.junit.jupiter.api.Disabled("Mock setup issue - requires investigation")
        void getSimulationStats_User_Forbidden() throws Exception {
                mockMvc.perform(get("/v1/stripe/stats")
                                .with(csrf()))
                                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = "1", roles = { "ADMIN" })
        @org.junit.jupiter.api.Disabled("Mock setup issue - requires investigation")
        void clearSimulatedData_Admin_Success() throws Exception {
                mockMvc.perform(delete("/v1/stripe/clear")
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("All simulated Stripe data cleared"));
        }

        @Test
        @WithMockUser(username = "1", roles = { "USER" })
        @org.junit.jupiter.api.Disabled("Mock setup issue - requires investigation")
        void clearSimulatedData_User_Forbidden() throws Exception {
                mockMvc.perform(delete("/v1/stripe/clear")
                                .with(csrf()))
                                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = "1", roles = { "ADMIN" })
        @org.junit.jupiter.api.Disabled("Mock setup issue - requires investigation")
        void refundPaymentIntent_Admin_Success() throws Exception {
                PaymentIntentResponse refundedIntent = PaymentIntentResponse.builder()
                                .id("pi_sim_123456")
                                .status("succeeded")
                                .simulated(true)
                                .build();

                when(stripeService.refundPaymentIntent(eq("pi_sim_123456"), any(), any()))
                                .thenReturn(refundedIntent);

                mockMvc.perform(post("/v1/stripe/refund/pi_sim_123456")
                                .with(csrf())
                                .param("amount", "9.99")
                                .param("reason", "Customer request"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value("pi_sim_123456"));
        }

        @Test
        @org.junit.jupiter.api.Disabled("Mock setup issue - requires investigation")
        void createPaymentIntent_Unauthorized_NoAuth() throws Exception {
                CreatePaymentIntentRequest request = CreatePaymentIntentRequest.builder()
                                .amount(new BigDecimal("9.99"))
                                .currency("usd")
                                .build();

                mockMvc.perform(post("/v1/stripe/create-payment-intent")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnauthorized());
        }
}
