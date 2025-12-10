package me.aydgn.MorseMate.service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Subscription;
import com.stripe.model.PaymentMethod;
import com.stripe.model.PaymentMethodCollection;
import com.stripe.net.RequestOptions;
import java.util.ArrayList;
import com.stripe.model.Customer;
import me.aydgn.MorseMate.entity.User;
import me.aydgn.MorseMate.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StripeService Tests")
class StripeServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private StripeService stripeService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();
    }

    @Test
    @DisplayName("Should create a new Stripe customer for a new user")
    void createCustomer_NewUser() throws StripeException {
        // Given
        user.setStripeCustomerId(null);

        Customer mockCustomer = new Customer();
        mockCustomer.setId("cus_123");

        try (MockedStatic<Customer> mockedCustomer = mockStatic(Customer.class)) {
            mockedCustomer.when(() -> Customer.create(anyMap())).thenReturn(mockCustomer);

            // When
            Customer result = stripeService.createCustomer(user);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("cus_123");
            assertThat(user.getStripeCustomerId()).isEqualTo("cus_123");
            verify(userRepository, times(1)).save(user);
        }
    }

    @Test
    @DisplayName("Should retrieve an existing Stripe customer")
    void createCustomer_ExistingUser() throws StripeException {
        // Given
        user.setStripeCustomerId("cus_existing");

        Customer mockCustomer = new Customer();
        mockCustomer.setId("cus_existing");

        try (MockedStatic<Customer> mockedCustomer = mockStatic(Customer.class)) {
            mockedCustomer.when(() -> Customer.retrieve("cus_existing")).thenReturn(mockCustomer);

            // When
            Customer result = stripeService.createCustomer(user);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("cus_existing");
            verify(userRepository, never()).save(any(User.class));
            mockedCustomer.verify(() -> Customer.create(anyMap()), never());
        }
    }

    @Test
    @DisplayName("Should create a PaymentIntent")
    void createPaymentIntent() throws StripeException {
        // Given
        PaymentIntent mockPaymentIntent = new PaymentIntent();
        mockPaymentIntent.setId("pi_123");

        try (MockedStatic<PaymentIntent> mockedPi = mockStatic(PaymentIntent.class)) {
            mockedPi.when(() -> PaymentIntent.create(anyMap(), any(RequestOptions.class))).thenReturn(mockPaymentIntent);

            // When
            PaymentIntent result = stripeService.createPaymentIntent(1000L, "usd", "cus_123");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("pi_123");
        }
    }

    @Test
    @DisplayName("Should create a Subscription")
    void createSubscription() throws StripeException {
        // Given
        Subscription mockSubscription = new Subscription();
        mockSubscription.setId("sub_123");

        try (MockedStatic<Subscription> mockedSub = mockStatic(Subscription.class)) {
            mockedSub.when(() -> Subscription.create(anyMap(), any(RequestOptions.class))).thenReturn(mockSubscription);

            // When
            Subscription result = stripeService.createSubscription("cus_12_3", "price_123");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("sub_123");
        }
    }

    @Test
    @DisplayName("Should cancel a Subscription")
    void cancelSubscription() throws StripeException {
        // Given
        Subscription mockSubscription = mock(Subscription.class);
        when(mockSubscription.cancel(anyMap())).thenReturn(mockSubscription);

        try (MockedStatic<Subscription> mockedSub = mockStatic(Subscription.class)) {
            mockedSub.when(() -> Subscription.retrieve(anyString())).thenReturn(mockSubscription);

            // When
            Subscription result = stripeService.cancelSubscription("sub_123");

            // Then
            assertThat(result).isNotNull();
            verify(mockSubscription, times(1)).cancel(anyMap());
        }
    }

    @Test
    @DisplayName("Should list PaymentMethods")
    void listPaymentMethods() throws StripeException {
        // Given
        PaymentMethodCollection mockPaymentMethods = new PaymentMethodCollection();
        mockPaymentMethods.setData(new ArrayList<>());

        try (MockedStatic<PaymentMethod> mockedPm = mockStatic(PaymentMethod.class)) {
            mockedPm.when(() -> PaymentMethod.list(anyMap())).thenReturn(mockPaymentMethods);

            // When
            PaymentMethodCollection result = stripeService.listPaymentMethods("cus_123");

            // Then
            assertThat(result).isNotNull();
        }
    }

    @Test
    @DisplayName("Should attach a PaymentMethod")
    void attachPaymentMethod() throws StripeException {
        // Given
        PaymentMethod mockPaymentMethod = mock(PaymentMethod.class);
        when(mockPaymentMethod.attach(anyMap())).thenReturn(mockPaymentMethod);

        try (MockedStatic<PaymentMethod> mockedPm = mockStatic(PaymentMethod.class)) {
            mockedPm.when(() -> PaymentMethod.retrieve(anyString())).thenReturn(mockPaymentMethod);

            // When
            PaymentMethod result = stripeService.attachPaymentMethod("cus_123", "pm_123");

            // Then
            assertThat(result).isNotNull();
            verify(mockPaymentMethod, times(1)).attach(anyMap());
        }
    }
}
