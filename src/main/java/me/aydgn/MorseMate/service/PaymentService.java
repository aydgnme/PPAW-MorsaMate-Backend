package me.aydgn.MorseMate.service;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.dto.PaymentHistoryDTO;
import me.aydgn.MorseMate.dto.PaymentRequestDTO;
import me.aydgn.MorseMate.dto.PaymentResponseDTO;
import me.aydgn.MorseMate.dto.PaymentStatsDTO;
import me.aydgn.MorseMate.dto.PaymentCardDTO;
import me.aydgn.MorseMate.dto.request.CreatePaymentIntentRequest;
import me.aydgn.MorseMate.dto.request.RefundPaymentRequest;
import me.aydgn.MorseMate.dto.response.PaymentIntentResponseDto;
import me.aydgn.MorseMate.dto.response.PaymentResponse;
import me.aydgn.MorseMate.entity.Payment;
import me.aydgn.MorseMate.entity.User;
import me.aydgn.MorseMate.entity.SubscriptionPlan;
import me.aydgn.MorseMate.entity.UserSubscription;
import me.aydgn.MorseMate.exception.InvalidOperationException;
import me.aydgn.MorseMate.exception.ResourceNotFoundException;
import me.aydgn.MorseMate.repository.PaymentRepository;
import me.aydgn.MorseMate.repository.SubscriptionPlanRepository;
import me.aydgn.MorseMate.repository.UserRepository;
import me.aydgn.MorseMate.repository.UserSubscriptionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for managing payments.
 *
 * SIMULATION MODE:
 * This service simulates payment processing without actual Stripe integration.
 * Real Stripe integration will be added in future iterations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final UserSubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final PaymentCardService cardService;
    private final UserSubscriptionService subscriptionService;
    private final StripeService stripeService;

    @Value("${payment.default-currency:USD}")
    private String defaultCurrency;

    // ===== Existing generic payment APIs (kept for compatibility) =====

    /**
     * Creates a new local payment record in a PENDING state.
     *
     * @param userId   The ID of the user initiating the payment.
     * @param request  The request containing amount, currency, and metadata.
     * @return The created Payment entity.
     */
    @Transactional
    public Payment createPayment(Long userId, CreatePaymentIntentRequest request) {
        log.info("Creating local payment record for user ID: {}, amount: {} {}",
                userId, request.getAmount(), request.getCurrency());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Payment payment = Payment.builder()
                .user(user)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(Payment.Status.PENDING)
                .transactionDate(LocalDateTime.now())
                .build();

        if (request.getSubscriptionId() != null) {
            UserSubscription subscription = subscriptionRepository.findById(request.getSubscriptionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with ID: " + request.getSubscriptionId()));
            payment.setSubscription(subscription);
        }

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Local payment record created with ID: {}", savedPayment.getId());

        return savedPayment;
    }

    /**
     * Processes a payment by creating and confirming a PaymentIntent on Stripe.
     *
     * @param paymentId The ID of the local payment record.
     * @param paymentMethodId The ID of the Stripe PaymentMethod to use.
     * @return The updated Payment entity.
     * @throws StripeException
     */
    @Transactional
    public Payment processPayment(Long paymentId, String paymentMethodId) throws StripeException {
        log.info("Processing payment for local payment ID: {}", paymentId);
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));

        if (payment.getStatus() != Payment.Status.PENDING) {
            throw new InvalidOperationException("Payment is not in PENDING state.");
        }

        User user = payment.getUser();
        Customer stripeCustomer = stripeService.createCustomer(user);

        // Create a PaymentIntent on Stripe
        Long amountInCents = payment.getAmount().multiply(new BigDecimal("100")).longValue();

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency(payment.getCurrency())
                .setCustomer(stripeCustomer.getId())
                .setPaymentMethod(paymentMethodId)
                .setConfirm(true) // Confirm the payment immediately
                .setOffSession(true) // For server-side confirmation
                .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params);

        payment.setStripePaymentId(paymentIntent.getId());

        // Handle the status of the payment intent
        String stripeStatus = paymentIntent.getStatus();
        log.info("Stripe Payment Intent {} status after processing: {}", paymentIntent.getId(), stripeStatus);
        
        switch (stripeStatus) {
            case "succeeded":
                payment.setStatus(Payment.Status.COMPLETED);
                break;
            case "requires_action":
            case "requires_source_action":
                // 3D Secure is required. The client will need to handle this.
                // For this example, we'll mark as pending and let the client handle it.
                // In a real app you might store the client secret of the PI and send it back.
                payment.setStatus(Payment.Status.PENDING);
                break;
            default:
                payment.setStatus(Payment.Status.FAILED);
                break;
        }

        return paymentRepository.save(payment);
    }

    /**
     * Validates that the payment amount is positive.
     *
     * @param amount The amount to validate.
     */
    public void validatePaymentAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException("Payment amount must be positive.");
        }
    }

    /**
     * Confirms a payment by checking its status with Stripe and updating the local database.
     *
     * @param paymentId The ID of the local payment record.
     * @return The updated payment response.
     * @throws StripeException If there's an error communicating with Stripe.
     */
    @Transactional
    public PaymentResponse confirmPayment(Long paymentId) throws StripeException {
        log.info("Confirming payment for local payment ID: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));

        if (payment.getStatus() == Payment.Status.COMPLETED) {
            log.warn("Payment with ID: {} is already completed.", paymentId);
            return mapToResponse(payment);
        }

        // Retrieve the Payment Intent from Stripe
        PaymentIntent paymentIntent = PaymentIntent.retrieve(payment.getStripePaymentId());

        String stripeStatus = paymentIntent.getStatus();
        log.info("Stripe Payment Intent {} status: {}", paymentIntent.getId(), stripeStatus);

        switch (stripeStatus) {
            case "succeeded":
                payment.setStatus(Payment.Status.COMPLETED);
                // In a real application, you would also handle fulfillment here
                // (e.g., granting access to a course, adding gems, etc.)
                break;
            case "processing":
                // Status is still pending, no change needed.
                break;
            case "requires_payment_method":
            case "failed":
                payment.setStatus(Payment.Status.FAILED);
                break;
            case "canceled":
                 payment.setStatus(Payment.Status.FAILED); // Or a new CANCELED status
                 break;
            default:
                log.warn("Unhandled Stripe Payment Intent status: {}", stripeStatus);
                break;
        }

        Payment updatedPayment = paymentRepository.save(payment);
        log.info("Updated local payment ID: {} to status: {}", updatedPayment.getId(), updatedPayment.getStatus());

        return mapToResponse(updatedPayment);
    }

    /**
     * Get payment by ID.
     *
     * @param paymentId Payment ID
     * @return Payment response
     */
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));

        return mapToResponse(payment);
    }

    /**
     * Get all payments for a user.
     *
     * @param userId User ID
     * @return List of user's payments
     */
    @Transactional(readOnly = true)
    public List<PaymentResponse> getUserPayments(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }

        List<Payment> payments = paymentRepository.findByUserId(userId);
        return payments.stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Get all payments for a subscription.
     *
     * @param subscriptionId Subscription ID
     * @return List of subscription's payments
     */
    @Transactional(readOnly = true)
    public List<PaymentResponse> getSubscriptionPayments(Long subscriptionId) {
        if (!subscriptionRepository.existsById(subscriptionId)) {
            throw new ResourceNotFoundException("Subscription not found with ID: " + subscriptionId);
        }

        List<Payment> payments = paymentRepository.findBySubscriptionId(subscriptionId);
        return payments.stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Get all payments (admin only).
     *
     * @param pageable Pagination parameters
     * @return Page of payments
     */
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getAllPayments(Pageable pageable) {
        Page<Payment> payments = paymentRepository.findAll(pageable);
        return payments.map(this::mapToResponse);
    }

    /**
     * Refund a payment.
     *
     * @param paymentId Payment ID to refund
     * @param request Refund details
     * @return Updated payment response
     */
    @Transactional
    public PaymentResponse refundPayment(Long paymentId, RefundPaymentRequest request) throws StripeException {
        log.info("Processing refund for payment ID: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));

        if (payment.getStatus() != Payment.Status.COMPLETED) {
            throw new InvalidOperationException(
                    "Can only refund completed payments. Current status: " + payment.getStatus());
        }

        Long amountToRefundInCents = null;
        if (request.getAmount() != null) {
            if (request.getAmount().compareTo(BigDecimal.ZERO) < 0) {
                throw new InvalidOperationException("Refund amount cannot be negative.");
            }
            if (request.getAmount().compareTo(payment.getAmount()) > 0) {
                throw new InvalidOperationException("Refund amount cannot be greater than payment amount.");
            }
            amountToRefundInCents = request.getAmount().multiply(new BigDecimal("100")).longValue();
        }

        // Perform refund via StripeService
        com.stripe.model.Refund refund = stripeService.refund(payment.getStripePaymentId(), amountToRefundInCents);

        Map<String, Object> metadata = payment.getMetadata();
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        metadata.put("refund_id", refund.getId());
        metadata.put("refund_amount", new BigDecimal(refund.getAmount()).movePointLeft(2));
        metadata.put("refund_date", LocalDateTime.now().toString());
        metadata.put("refund_reason", request.getReason());
        payment.setMetadata(metadata);
        
        // A full refund is when the refunded amount equals the original payment amount.
        if (amountToRefundInCents == null || amountToRefundInCents.equals(payment.getAmount().multiply(new BigDecimal("100")).longValue())) {
            payment.setStatus(Payment.Status.REFUNDED);
        }

        Payment refundedPayment = paymentRepository.save(payment);
        log.info("Payment ID: {} refunded successfully. Stripe Refund ID: {}",
                paymentId, refund.getId());

        return mapToResponse(refundedPayment);
    }

    /**
     * Get payment statistics for a user.
     *
     * @param userId User ID
     * @return Payment statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getUserPaymentStats(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }

        List<Payment> payments = paymentRepository.findByUserId(userId);

        long totalPayments = payments.size();
        long completedPayments = payments.stream()
                .filter(p -> p.getStatus() == Payment.Status.COMPLETED)
                .count();
        long failedPayments = payments.stream()
                .filter(p -> p.getStatus() == Payment.Status.FAILED)
                .count();
        long refundedPayments = payments.stream()
                .filter(p -> p.getStatus() == Payment.Status.REFUNDED)
                .count();

        BigDecimal totalSpent = payments.stream()
                .filter(p -> p.getStatus() == Payment.Status.COMPLETED)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRefunded = payments.stream()
                .filter(p -> p.getStatus() == Payment.Status.REFUNDED)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPayments", totalPayments);
        stats.put("completedPayments", completedPayments);
        stats.put("failedPayments", failedPayments);
        stats.put("refundedPayments", refundedPayments);
        stats.put("totalSpent", totalSpent);
        stats.put("totalRefunded", totalRefunded);
        stats.put("netSpent", totalSpent.subtract(totalRefunded));

        return stats;
    }

    /**
     * Map Payment entity to PaymentResponse DTO.
     */
    public PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .userId(payment.getUser().getId())
                .username(payment.getUser().getUsername())
                .subscriptionId(payment.getSubscription() != null
                        ? payment.getSubscription().getId()
                        : null)
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus().name())
                .paymentMethod(payment.getPaymentMethod())
                .stripePaymentId(payment.getStripePaymentId())
                .transactionDate(payment.getTransactionDate())
                .metadata(payment.getMetadata())
                .simulated(true)
                .build();
    }

    // ===== Card-based simulated subscription payments =====

    /**
     * Charge a subscription using either a saved card or raw card details.
     * This method is purely simulated and does not talk to real gateways.
     */
    @Transactional
    public PaymentResponseDTO charge(Long userId, PaymentRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Determine plan:
        // - If planId is provided in the request, use that (new purchase / upgrade case)
        // - Otherwise, fall back to the user's active subscription plan (renewal case)
        SubscriptionPlan plan;
        if (request.getPlanId() != null) {
            plan = subscriptionPlanRepository.findById(request.getPlanId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Plan not found with ID: " + request.getPlanId()));
        } else {
            UserSubscription subscription = subscriptionRepository.findWithPlanByUserId(userId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Active subscription not found for user: " + userId));
            plan = subscription.getPlan();
        }

        // Determine amount / currency from request or subscription plan
        var amount = request.getAmount() != null ? request.getAmount() : plan.getPrice();
        var currency = request.getCurrency() != null ? request.getCurrency() : defaultCurrency;

        // Validate amount quickly
        if (amount == null || amount.signum() <= 0) {
            throw new InvalidOperationException("Amount must be positive");
        }

        // Get or create card
        PaymentCardDTO cardDto;
        if (request.getCardId() != null) {
            // Use specified card
            cardDto = cardService.listCards(userId).stream()
                    .filter(c -> c.getId().equals(request.getCardId()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Card not found"));
        } else if (request.getCardNumber() != null && request.getCvv() != null) {
            // Add new card from raw data
            validateCardNumber(request.getCardNumber());
            validateCvv(request.getCvv());
            cardDto = cardService.addCard(
                    userId,
                    request.getCardholderName(),
                    request.getCardNumber(),
                    request.getCvv(),
                    request.getExpiryMonth(),
                    request.getExpiryYear(),
                    request.isSaveCard()
            );
        } else {
            // Try to use default card from saved cards
            List<PaymentCardDTO> savedCards = cardService.listCards(userId);
            if (savedCards.isEmpty()) {
                throw new InvalidOperationException(
                        "No saved card found. Please provide cardId or card information.");
            }
            // Use default card (first in list is default due to ordering)
            cardDto = savedCards.get(0);
            log.info("Using default card ID: {} for user ID: {}", cardDto.getId(), userId);
        }

        boolean success = simulateCharge(amount);

        String transactionRef = "SIM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        LocalDateTime processedAt = LocalDateTime.now();

        // If payment succeeded and planId is provided, create/update subscription
        UserSubscription subscription = null;
        if (success && request.getPlanId() != null) {
            try {
                // Check if user already has an active subscription
                var existingSubscription = subscriptionRepository.findWithPlanByUserId(userId);
                if (existingSubscription.isPresent() && existingSubscription.get().getStatus() == UserSubscription.Status.ACTIVE) {
                    // Upgrade existing subscription
                    var subscriptionResponse = subscriptionService.upgradeSubscription(userId, request.getPlanId());
                    subscription = subscriptionRepository.findById(subscriptionResponse.getId())
                            .orElseThrow(() -> new ResourceNotFoundException("Subscription not found after upgrade"));
                    log.info("Subscription upgraded for user ID: {}, subscription ID: {}", userId, subscription.getId());
                } else {
                    // Create new subscription
                    var subscriptionRequest = me.aydgn.MorseMate.dto.request.UserSubscriptionRequest.builder()
                            .planId(request.getPlanId())
                            .autoRenew(true)
                            .build();
                    var subscriptionResponse = subscriptionService.subscribeUser(userId, subscriptionRequest);
                    subscription = subscriptionRepository.findById(subscriptionResponse.getId())
                            .orElseThrow(() -> new ResourceNotFoundException("Subscription not found after creation"));
                    log.info("Subscription created for user ID: {}, subscription ID: {}", userId, subscription.getId());
                }
            } catch (Exception e) {
                log.error("Failed to create/update subscription after payment: {}", e.getMessage());
                // Continue with payment creation even if subscription creation fails
            }
        } else if (success) {
            // For renewal case, get existing subscription
            subscription = subscriptionRepository.findWithPlanByUserId(userId).orElse(null);
        }

        // Create Payment entity for history
        Payment.Status paymentStatus = success ? Payment.Status.COMPLETED : Payment.Status.FAILED;
        String stripePaymentId = success ? "sim_" + UUID.randomUUID().toString().substring(0, 24) : null;

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("simulated", true);
        metadata.put("transaction_ref", transactionRef);
        metadata.put("card_id", cardDto.getId());
        metadata.put("card_last4", cardDto.getCardLast4());
        metadata.put("card_brand", cardDto.getCardBrand());

        Payment payment = Payment.builder()
                .user(user)
                .subscription(subscription)
                .amount(amount)
                .currency(currency)
                .status(paymentStatus)
                .paymentMethod("credit_card")
                .stripePaymentId(stripePaymentId)
                .transactionDate(processedAt)
                .metadata(metadata)
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment saved with ID: {}, status: {}", savedPayment.getId(), savedPayment.getStatus());

        return PaymentResponseDTO.builder()
                .status(success ? "SUCCESS" : "FAILED")
                .failureReason(success ? null : "Simulated insufficient funds")
                .amount(amount)
                .currency(currency)
                .planId(plan.getId())
                .planName(plan.getName())
                .transactionRef(transactionRef)
                .processedAt(processedAt)
                .cardId(cardDto.getId())
                .card(cardDto)
                .build();
    }

    @Transactional(readOnly = true)
    public List<PaymentHistoryDTO> getPaymentHistory(Long userId) {
        // Get all payments for this user, ordered by transaction date (newest first)
        List<Payment> payments = paymentRepository.findByUserId(userId);

        return payments.stream()
                .map(payment -> {
                    String type = "INITIAL";
                    if (payment.getSubscription() != null) {
                        // Determine type based on subscription history
                        // For simplicity, we'll use INITIAL for now
                        // In a real system, you'd check if this is a renewal/upgrade
                        type = "RENEWAL";
                    }

                    Long planId = null;
                    String planName = null;
                    if (payment.getSubscription() != null && payment.getSubscription().getPlan() != null) {
                        planId = payment.getSubscription().getPlan().getId();
                        planName = payment.getSubscription().getPlan().getName();
                    }

                    return PaymentHistoryDTO.builder()
                            .subscriptionId(payment.getSubscription() != null ? payment.getSubscription().getId() : null)
                            .planId(planId)
                            .planName(planName)
                            .amount(payment.getAmount())
                            .currency(payment.getCurrency())
                            .type(type)
                            .status(payment.getStatus() == Payment.Status.COMPLETED ? "SUCCESS" : "FAILED")
                            .createdAt(payment.getTransactionDate())
                            .build();
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public PaymentStatsDTO getPaymentStats(Long userId) {
        List<PaymentHistoryDTO> history = getPaymentHistory(userId);
        long total = history.size();
        long success = history.stream().filter(h -> "SUCCESS".equals(h.getStatus())).count();

        BigDecimal totalAmount = history.stream()
                .map(PaymentHistoryDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avg = total > 0
                ? totalAmount.divide(BigDecimal.valueOf(total), 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        long activeSubs = subscriptionRepository.findByStatus(UserSubscription.Status.ACTIVE).size();

        return PaymentStatsDTO.builder()
                .totalPayments(total)
                .successfulPayments(success)
                .failedPayments(total - success)
                .totalAmount(totalAmount)
                .averageAmount(avg)
                .activeSubscriptions(activeSubs)
                .build();
    }

    // ===== helpers for simulated charge =====

    private void validateCardNumber(String cardNumber) {
        String digits = cardNumber.replaceAll("\\s+", "");
        if (digits.length() < 13 || digits.length() > 19 || !luhnCheck(digits)) {
            throw new InvalidOperationException("Invalid card number");
        }
    }

    private boolean luhnCheck(String digits) {
        int sum = 0;
        boolean alternate = false;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int n = digits.charAt(i) - '0';
            if (alternate) {
                n *= 2;
                if (n > 9) n -= 9;
            }
            sum += n;
            alternate = !alternate;
        }
        return sum % 10 == 0;
    }

    private void validateCvv(String cvv) {
        if (cvv == null || !cvv.matches("\\d{3,4}")) {
            throw new InvalidOperationException("Invalid CVV");
        }
    }

    private boolean simulateCharge(BigDecimal amount) {
        // Very naive heuristic: if cents part ends with 7, fail.
        int cents = amount.movePointRight(2).remainder(BigDecimal.valueOf(100)).intValue();
        return cents % 10 != 7;
    }
}

