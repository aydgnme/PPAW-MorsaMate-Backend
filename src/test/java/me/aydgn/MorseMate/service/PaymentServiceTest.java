package me.aydgn.MorseMate.service;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import me.aydgn.MorseMate.dto.request.CreatePaymentIntentRequest;
import me.aydgn.MorseMate.dto.request.RefundPaymentRequest;
import me.aydgn.MorseMate.dto.response.PaymentResponse;
import me.aydgn.MorseMate.entity.Payment;
import me.aydgn.MorseMate.entity.User;
import me.aydgn.MorseMate.repository.PaymentRepository;
import me.aydgn.MorseMate.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService Tests")
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StripeService stripeService;

    @Mock
    private me.aydgn.MorseMate.repository.UserSubscriptionRepository userSubscriptionRepository;

    @Mock
    private me.aydgn.MorseMate.repository.SubscriptionPlanRepository subscriptionPlanRepository;

    @Mock
    private me.aydgn.MorseMate.service.PaymentCardService paymentCardService;

    @Mock
    private me.aydgn.MorseMate.service.UserSubscriptionService userSubscriptionService;

    @InjectMocks
    private PaymentService paymentService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .stripeCustomerId("cus_123")
                .build();
    }

    @Test
    @DisplayName("Should create local payment record successfully")
    void createPayment_Success() {
        // Given
        CreatePaymentIntentRequest request = CreatePaymentIntentRequest.builder()
                .amount(new BigDecimal("10.00"))
                .currency("usd")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });

        // When
        Payment payment = paymentService.createPayment(1L, request);

        // Then
        assertThat(payment).isNotNull();
        assertThat(payment.getId()).isEqualTo(1L);
        assertThat(payment.getStatus()).isEqualTo(Payment.Status.PENDING);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should process payment successfully")
    void processPayment_Success() throws StripeException {
        // Given
        Payment payment = Payment.builder()
                .id(1L)
                .amount(new BigDecimal("10.00"))
                .currency("usd")
                .status(Payment.Status.PENDING)
                .build();
        payment.setUser(user);
        
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(stripeService.createCustomer(user)).thenReturn(new Customer());

        PaymentIntent mockPaymentIntent = new PaymentIntent();
        mockPaymentIntent.setId("pi_123");
        mockPaymentIntent.setStatus("succeeded");

        try (var mockedPi = mockStatic(PaymentIntent.class)) {
            mockedPi.when(() -> PaymentIntent.create(anyMap())).thenReturn(mockPaymentIntent);
            
            when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Payment processedPayment = paymentService.processPayment(1L, "pm_123");

            // Then
            assertThat(processedPayment).isNotNull();
            assertThat(processedPayment.getStatus()).isEqualTo(Payment.Status.COMPLETED);
            assertThat(processedPayment.getStripePaymentId()).isEqualTo("pi_123");
            verify(paymentRepository, times(1)).save(processedPayment);
        }
    }


    @Test
    @DisplayName("Should confirm payment successfully")
    void confirmPayment_Success() throws StripeException {
        // Given
        Payment payment = Payment.builder()
                .id(1L)
                .user(user)
                .stripePaymentId("pi_123")
                .status(Payment.Status.PENDING)
                .build();

        PaymentIntent mockPaymentIntent = new PaymentIntent();
        mockPaymentIntent.setId("pi_123");
        mockPaymentIntent.setStatus("succeeded");

        try (var mockedPi = mockStatic(PaymentIntent.class)) {
            mockedPi.when(() -> PaymentIntent.retrieve("pi_123")).thenReturn(mockPaymentIntent);

            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
            when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            PaymentResponse response = paymentService.confirmPayment(1L);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(Payment.Status.COMPLETED.name());
            verify(paymentRepository, times(1)).save(payment);
        }
    }

    @Test
    @DisplayName("Should refund payment successfully")
    void refundPayment_Success() throws StripeException {
        // Given
        Payment payment = Payment.builder()
                .id(1L)
                .user(user)
                .amount(new BigDecimal("10.00"))
                .stripePaymentId("pi_123")
                .status(Payment.Status.COMPLETED)
                .build();

        Refund mockRefund = new Refund();
        mockRefund.setId("re_123");
        mockRefund.setAmount(1000L);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(stripeService.refund(eq("pi_123"), any())).thenReturn(mockRefund);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        PaymentResponse response = paymentService.refundPayment(1L, new RefundPaymentRequest());

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(Payment.Status.REFUNDED.name());
        verify(paymentRepository, times(1)).save(payment);
    }
}