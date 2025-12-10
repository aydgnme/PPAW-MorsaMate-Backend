package me.aydgn.MorseMate.service;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import me.aydgn.MorseMate.dto.request.CreatePaymentIntentRequest;
import me.aydgn.MorseMate.dto.request.RefundPaymentRequest;
import me.aydgn.MorseMate.dto.response.PaymentIntentResponseDto;
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
    @DisplayName("Should create PaymentIntent successfully")
    void createPaymentIntent_Success() throws StripeException {
        // Given
        CreatePaymentIntentRequest request = CreatePaymentIntentRequest.builder()
                .amount(new BigDecimal("10.00"))
                .currency("usd")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Customer mockCustomer = new Customer();
        mockCustomer.setId("cus_123");
        when(stripeService.createCustomer(user)).thenReturn(mockCustomer);

        PaymentIntent mockPaymentIntent = new PaymentIntent();
        mockPaymentIntent.setId("pi_123");
        mockPaymentIntent.setClientSecret("pi_123_secret");
        when(stripeService.createPaymentIntent(1000L, "usd", "cus_123")).thenReturn(mockPaymentIntent);

        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        PaymentIntentResponseDto response = paymentService.createPaymentIntent(1L, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getClientSecret()).isEqualTo("pi_123_secret");
        verify(paymentRepository, times(1)).save(any(Payment.class));
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
                .stripePaymentId("pi_123")
                .status(Payment.Status.COMPLETED)
                .build();

        Refund mockRefund = new Refund();
        mockRefund.setId("re_123");
        mockRefund.setAmount(1000L);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(stripeService.refund("pi_123")).thenReturn(mockRefund);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        PaymentResponse response = paymentService.refundPayment(1L, new RefundPaymentRequest());

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(Payment.Status.REFUNDED.name());
        verify(paymentRepository, times(1)).save(payment);
    }
}
