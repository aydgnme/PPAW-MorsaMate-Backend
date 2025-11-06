package me.aydgn.MorseMate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.dto.request.CreatePaymentRequest;
import me.aydgn.MorseMate.dto.request.RefundPaymentRequest;
import me.aydgn.MorseMate.dto.response.PaymentResponse;
import me.aydgn.MorseMate.entity.Payment;
import me.aydgn.MorseMate.entity.User;
import me.aydgn.MorseMate.entity.UserSubscription;
import me.aydgn.MorseMate.exception.InvalidOperationException;
import me.aydgn.MorseMate.exception.ResourceNotFoundException;
import me.aydgn.MorseMate.repository.PaymentRepository;
import me.aydgn.MorseMate.repository.UserRepository;
import me.aydgn.MorseMate.repository.UserSubscriptionRepository;
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

    /**
     * Create a new payment (simulated).
     *
     * @param userId User ID making the payment
     * @param request Payment details
     * @return Created payment response
     */
    @Transactional
    public PaymentResponse createPayment(Long userId, CreatePaymentRequest request) {
        log.info("Creating simulated payment for user ID: {}, amount: {} {}",
                userId, request.getAmount(), request.getCurrency());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Validate subscription if provided
        UserSubscription subscription = null;
        if (request.getSubscriptionId() != null) {
            subscription = subscriptionRepository.findById(request.getSubscriptionId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Subscription not found with ID: " + request.getSubscriptionId()));

            if (!subscription.getUser().getId().equals(userId)) {
                throw new InvalidOperationException("Subscription does not belong to this user");
            }
        }

        // Simulate payment processing
        Payment.Status status;
        String stripePaymentId = null;

        if (request.getSimulateFailure() != null && request.getSimulateFailure()) {
            status = Payment.Status.FAILED;
            log.warn("Simulating payment failure for user ID: {}", userId);
        } else {
            status = Payment.Status.COMPLETED;
            stripePaymentId = "sim_" + UUID.randomUUID().toString().substring(0, 24);
            log.info("Payment simulation successful. Simulated ID: {}", stripePaymentId);
        }

        // Create metadata
        Map<String, Object> metadata = new HashMap<>();
        if (request.getMetadata() != null) {
            metadata.putAll(request.getMetadata());
        }
        metadata.put("simulated", true);
        metadata.put("simulation_timestamp", LocalDateTime.now().toString());

        // Create payment entity
        Payment payment = Payment.builder()
                .user(user)
                .subscription(subscription)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(status)
                .paymentMethod(request.getPaymentMethod())
                .stripePaymentId(stripePaymentId)
                .transactionDate(LocalDateTime.now())
                .metadata(metadata)
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment created with ID: {}, status: {}", savedPayment.getId(), savedPayment.getStatus());

        return mapToResponse(savedPayment);
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
     * Refund a payment (simulated).
     *
     * @param paymentId Payment ID to refund
     * @param request Refund details
     * @return Updated payment response
     */
    @Transactional
    public PaymentResponse refundPayment(Long paymentId, RefundPaymentRequest request) {
        log.info("Processing refund for payment ID: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));

        if (payment.getStatus() != Payment.Status.COMPLETED) {
            throw new InvalidOperationException(
                    "Can only refund completed payments. Current status: " + payment.getStatus());
        }

        if (payment.getStatus() == Payment.Status.REFUNDED) {
            throw new InvalidOperationException("Payment has already been refunded");
        }

        // Validate refund amount
        BigDecimal refundAmount = request.getAmount() != null
                ? request.getAmount()
                : payment.getAmount();

        if (refundAmount.compareTo(payment.getAmount()) > 0) {
            throw new InvalidOperationException("Refund amount cannot exceed original payment amount");
        }

        // Update payment status
        payment.setStatus(Payment.Status.REFUNDED);

        // Add refund info to metadata
        Map<String, Object> metadata = payment.getMetadata();
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        metadata.put("refund_amount", refundAmount.toString());
        metadata.put("refund_date", LocalDateTime.now().toString());
        metadata.put("refund_reason", request.getReason());
        metadata.put("refund_simulated", true);
        payment.setMetadata(metadata);

        Payment refundedPayment = paymentRepository.save(payment);
        log.info("Payment ID: {} refunded successfully. Amount: {} {}",
                paymentId, refundAmount, payment.getCurrency());

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
    private PaymentResponse mapToResponse(Payment payment) {
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
}
