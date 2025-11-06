package me.aydgn.MorseMate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.dto.request.CreatePaymentRequest;
import me.aydgn.MorseMate.dto.request.RefundPaymentRequest;
import me.aydgn.MorseMate.dto.response.PaymentResponse;
import me.aydgn.MorseMate.service.PaymentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for payment management.
 *
 * SIMULATION MODE:
 * This controller handles simulated payments without actual Stripe integration.
 * All payments are mock transactions for testing and development purposes.
 * Real Stripe integration will be added in future iterations.
 *
 * Endpoints:
 * - POST   /v1/payments/create          - Create a new payment (simulated)
 * - GET    /v1/payments/my              - Get current user's payments
 * - GET    /v1/payments/my/stats        - Get payment statistics for current user
 * - GET    /v1/payments/{id}            - Get payment details
 * - POST   /v1/payments/{id}/refund     - Refund a payment (admin only)
 * - GET    /v1/payments                 - Get all payments (admin only)
 * - GET    /v1/payments/users/{userId}  - Get user's payments (admin only)
 * - GET    /v1/payments/subscriptions/{subscriptionId} - Get subscription payments
 */
@RestController
@RequestMapping("/${api.version}/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Create a new payment (simulated).
     *
     * Example request:
     * POST /v1/payments/create
     * {
     *   "subscriptionId": 1,
     *   "amount": 9.99,
     *   "currency": "USD",
     *   "paymentMethod": "credit_card",
     *   "simulateFailure": false
     * }
     *
     * @param request Payment details
     * @return Created payment response
     */
    @PostMapping("/create")
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        Long userId = getCurrentUserId();
        log.info("User ID: {} creating payment for amount: {} {}",
                userId, request.getAmount(), request.getCurrency());

        PaymentResponse payment = paymentService.createPayment(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }

    /**
     * Get current user's payments.
     *
     * @return List of user's payments
     */
    @GetMapping("/my")
    public ResponseEntity<List<PaymentResponse>> getMyPayments() {
        Long userId = getCurrentUserId();
        log.debug("Fetching payments for user ID: {}", userId);

        List<PaymentResponse> payments = paymentService.getUserPayments(userId);
        return ResponseEntity.ok(payments);
    }

    /**
     * Get payment statistics for current user.
     *
     * Returns statistics including:
     * - Total payments count
     * - Completed payments count
     * - Failed payments count
     * - Total amount spent
     * - Total refunded
     *
     * @return Payment statistics
     */
    @GetMapping("/my/stats")
    public ResponseEntity<Map<String, Object>> getMyPaymentStats() {
        Long userId = getCurrentUserId();
        log.debug("Fetching payment statistics for user ID: {}", userId);

        Map<String, Object> stats = paymentService.getUserPaymentStats(userId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get payment by ID.
     * Users can only access their own payments.
     * Admins can access any payment.
     *
     * @param id Payment ID
     * @return Payment details
     */
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable("id") Long id) {
        log.debug("Fetching payment with ID: {}", id);

        PaymentResponse payment = paymentService.getPayment(id);

        // Check if user owns this payment (unless admin)
        Long currentUserId = getCurrentUserId();
        if (!payment.getUserId().equals(currentUserId) && !isAdmin()) {
            log.warn("User ID: {} attempted to access payment ID: {} owned by user ID: {}",
                    currentUserId, id, payment.getUserId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(payment);
    }

    /**
     * Get payments for a specific subscription.
     *
     * @param subscriptionId Subscription ID
     * @return List of subscription's payments
     */
    @GetMapping("/subscriptions/{subscriptionId}")
    public ResponseEntity<List<PaymentResponse>> getSubscriptionPayments(
            @PathVariable("subscriptionId") Long subscriptionId) {

        log.debug("Fetching payments for subscription ID: {}", subscriptionId);

        List<PaymentResponse> payments = paymentService.getSubscriptionPayments(subscriptionId);
        return ResponseEntity.ok(payments);
    }

    // ========== ADMIN ENDPOINTS ==========

    /**
     * Get all payments (admin only).
     *
     * @param page Page number (default: 0)
     * @param size Page size (default: 20, max: 100)
     * @param sortBy Sort field (default: transactionDate)
     * @param sortDir Sort direction (default: DESC)
     * @return Page of payments
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<PaymentResponse>> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        log.debug("Admin fetching all payments - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(
                page,
                Math.min(size, 100),
                Sort.Direction.fromString(sortDir),
                sortBy
        );

        Page<PaymentResponse> payments = paymentService.getAllPayments(pageable);
        return ResponseEntity.ok(payments);
    }

    /**
     * Get payments for any user (admin only).
     *
     * @param userId User ID
     * @return List of user's payments
     */
    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentResponse>> getUserPayments(@PathVariable("userId") Long userId) {
        log.debug("Admin fetching payments for user ID: {}", userId);

        List<PaymentResponse> payments = paymentService.getUserPayments(userId);
        return ResponseEntity.ok(payments);
    }

    /**
     * Get payment statistics for any user (admin only).
     *
     * @param userId User ID
     * @return Payment statistics
     */
    @GetMapping("/users/{userId}/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getUserPaymentStats(@PathVariable("userId") Long userId) {
        log.debug("Admin fetching payment statistics for user ID: {}", userId);

        Map<String, Object> stats = paymentService.getUserPaymentStats(userId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Refund a payment (admin only, simulated).
     *
     * Example request:
     * POST /v1/payments/123/refund
     * {
     *   "amount": 9.99,
     *   "reason": "Customer request"
     * }
     *
     * @param id Payment ID
     * @param request Refund details (optional amount and reason)
     * @return Updated payment with refund information
     */
    @PostMapping("/{id}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> refundPayment(
            @PathVariable("id") Long id,
            @RequestBody(required = false) RefundPaymentRequest request) {

        log.info("Admin refunding payment ID: {}", id);

        if (request == null) {
            request = new RefundPaymentRequest();
        }

        PaymentResponse payment = paymentService.refundPayment(id, request);
        return ResponseEntity.ok(payment);
    }

    // ========== HELPER METHODS ==========

    /**
     * Get current user ID from security context.
     *
     * @return Current user ID
     * @throws IllegalStateException if user is not authenticated
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }
        return Long.parseLong(authentication.getName());
    }

    /**
     * Check if current user has ADMIN role.
     *
     * @return true if user is admin
     */
    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }
}
