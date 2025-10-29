package me.aydgn.MorseMate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.dto.request.AddGemsRequest;
import me.aydgn.MorseMate.dto.request.SpendGemsRequest;
import me.aydgn.MorseMate.dto.response.ApiMessage;
import me.aydgn.MorseMate.dto.response.GemTransactionResponse;
import me.aydgn.MorseMate.dto.response.UserGemsResponse;
import me.aydgn.MorseMate.entity.GemTransaction;
import me.aydgn.MorseMate.service.GemService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Gem management
 * Provides endpoints for managing gems and transactions
 */
@RestController
@RequestMapping("/${api.version}/gems")
@RequiredArgsConstructor
@Slf4j
public class GemController {

    private final GemService gemService;

    /**
     * GET /v1/gems/me
     * Get authenticated user's gem balance
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserGemsResponse> getMyGems(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        log.debug("GET /v1/gems/me - Fetching gems for user id: {}", userId);
        UserGemsResponse gems = gemService.getUserGems(userId);
        return ResponseEntity.ok(gems);
    }

    /**
     * POST /v1/gems/me/spend
     * Spend gems for authenticated user
     */
    @PostMapping("/me/spend")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserGemsResponse> spendMyGems(
            Authentication authentication,
            @Valid @RequestBody SpendGemsRequest request) {
        Long userId = Long.parseLong(authentication.getName());
        log.info("POST /v1/gems/me/spend - User {} spending {} gems for {}",
                userId, request.getAmount(), request.getSource());

        UserGemsResponse gems = gemService.spendGems(
                userId, request.getAmount(), request.getSource(), request.getDescription());
        return ResponseEntity.ok(gems);
    }

    /**
     * GET /v1/gems/me/transactions
     * Get authenticated user's gem transactions
     */
    @GetMapping("/me/transactions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<GemTransactionResponse>> getMyTransactions(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        log.debug("GET /v1/gems/me/transactions - Fetching transactions for user id: {}", userId);
        List<GemTransactionResponse> transactions = gemService.getUserTransactions(userId);
        return ResponseEntity.ok(transactions);
    }

    /**
     * GET /v1/gems/me/transactions/paged
     * Get authenticated user's gem transactions with pagination
     */
    @GetMapping("/me/transactions/paged")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<GemTransactionResponse>> getMyTransactionsPaged(
            Authentication authentication,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Long userId = Long.parseLong(authentication.getName());
        log.debug("GET /v1/gems/me/transactions/paged - Fetching paged transactions for user id: {}", userId);
        Page<GemTransactionResponse> transactions = gemService.getUserTransactionsPaged(userId, pageable);
        return ResponseEntity.ok(transactions);
    }

    /**
     * GET /v1/gems/me/transactions/type/{type}
     * Get authenticated user's gem transactions by type
     */
    @GetMapping("/me/transactions/type/{type}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<GemTransactionResponse>> getMyTransactionsByType(
            Authentication authentication,
            @PathVariable String type) {
        Long userId = Long.parseLong(authentication.getName());
        log.debug("GET /v1/gems/me/transactions/type/{} - Fetching for user id: {}", type, userId);

        GemTransaction.Type transactionType = GemTransaction.Type.valueOf(type.toUpperCase());
        List<GemTransactionResponse> transactions = gemService.getUserTransactionsByType(userId, transactionType);
        return ResponseEntity.ok(transactions);
    }

    /**
     * GET /v1/gems/me/transactions/range
     * Get authenticated user's gem transactions in time range
     */
    @GetMapping("/me/transactions/range")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<GemTransactionResponse>> getMyTransactionsInRange(
            Authentication authentication,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        Long userId = Long.parseLong(authentication.getName());
        log.debug("GET /v1/gems/me/transactions/range - User {} from {} to {}", userId, from, to);

        List<GemTransactionResponse> transactions = gemService.getUserTransactionsInRange(userId, from, to);
        return ResponseEntity.ok(transactions);
    }

    /**
     * GET /v1/gems/me/statistics
     * Get authenticated user's gem statistics
     */
    @GetMapping("/me/statistics")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getMyStatistics(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        log.debug("GET /v1/gems/me/statistics - Fetching statistics for user id: {}", userId);
        Map<String, Object> stats = gemService.getUserTransactionStatistics(userId);
        return ResponseEntity.ok(stats);
    }

    /**
     * GET /v1/gems/me/check/{amount}
     * Check if authenticated user has enough gems
     */
    @GetMapping("/me/check/{amount}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Boolean>> checkMyBalance(
            Authentication authentication,
            @PathVariable @Min(1) Integer amount) {
        Long userId = Long.parseLong(authentication.getName());
        log.debug("GET /v1/gems/me/check/{} - Checking for user id: {}", amount, userId);
        boolean hasEnough = gemService.hasEnoughGems(userId, amount);
        return ResponseEntity.ok(Map.of("hasEnough", hasEnough));
    }

    /**
     * GET /v1/gems/me/transactions/count
     * Get authenticated user's transaction count
     */
    @GetMapping("/me/transactions/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Long>> getMyTransactionCount(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        log.debug("GET /v1/gems/me/transactions/count - Counting for user id: {}", userId);
        long count = gemService.getUserTransactionCount(userId);
        return ResponseEntity.ok(Map.of("transactionCount", count));
    }

    /**
     * GET /v1/gems/me/net-amount/range
     * Get net gem amount in time range
     */
    @GetMapping("/me/net-amount/range")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Integer>> getMyNetAmountInRange(
            Authentication authentication,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        Long userId = Long.parseLong(authentication.getName());
        log.debug("GET /v1/gems/me/net-amount/range - User {} from {} to {}", userId, from, to);

        Integer netAmount = gemService.getNetAmountInRange(userId, from, to);
        return ResponseEntity.ok(Map.of("netAmount", netAmount));
    }

    // ==================== Admin Endpoints ====================

    /**
     * POST /v1/gems/users/{userId}/add
     * Add gems to specific user (Admin only)
     */
    @PostMapping("/users/{userId}/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserGemsResponse> addGemsToUser(
            @PathVariable Long userId,
            @Valid @RequestBody AddGemsRequest request) {
        log.info("POST /v1/gems/users/{}/add - Admin adding {} gems", userId, request.getAmount());

        UserGemsResponse gems = gemService.addGems(
                userId, request.getAmount(), request.getSource(), request.getDescription());
        return ResponseEntity.ok(gems);
    }

    /**
     * POST /v1/gems/users/{userId}/spend
     * Spend gems for specific user (Admin only)
     */
    @PostMapping("/users/{userId}/spend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserGemsResponse> spendGemsForUser(
            @PathVariable Long userId,
            @Valid @RequestBody SpendGemsRequest request) {
        log.info("POST /v1/gems/users/{}/spend - Admin spending {} gems", userId, request.getAmount());

        UserGemsResponse gems = gemService.spendGems(
                userId, request.getAmount(), request.getSource(), request.getDescription());
        return ResponseEntity.ok(gems);
    }

    /**
     * POST /v1/gems/users/{userId}/bonus
     * Award bonus gems to specific user (Admin only)
     */
    @PostMapping("/users/{userId}/bonus")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserGemsResponse> awardBonusToUser(
            @PathVariable Long userId,
            @Valid @RequestBody AddGemsRequest request) {
        log.info("POST /v1/gems/users/{}/bonus - Admin awarding {} bonus gems", userId, request.getAmount());

        UserGemsResponse gems = gemService.awardBonusGems(
                userId, request.getAmount(), request.getDescription());
        return ResponseEntity.ok(gems);
    }

    /**
     * GET /v1/gems/users/{userId}
     * Get specific user's gem balance (Admin only)
     */
    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserGemsResponse> getUserGems(@PathVariable Long userId) {
        log.debug("GET /v1/gems/users/{} - Admin fetching gems", userId);
        UserGemsResponse gems = gemService.getUserGems(userId);
        return ResponseEntity.ok(gems);
    }

    /**
     * GET /v1/gems/users/{userId}/transactions
     * Get specific user's transactions (Admin only)
     */
    @GetMapping("/users/{userId}/transactions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<GemTransactionResponse>> getUserTransactions(@PathVariable Long userId) {
        log.debug("GET /v1/gems/users/{}/transactions - Admin fetching", userId);
        List<GemTransactionResponse> transactions = gemService.getUserTransactions(userId);
        return ResponseEntity.ok(transactions);
    }

    /**
     * GET /v1/gems/users/{userId}/statistics
     * Get specific user's gem statistics (Admin only)
     */
    @GetMapping("/users/{userId}/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getUserStatistics(@PathVariable Long userId) {
        log.debug("GET /v1/gems/users/{}/statistics - Admin fetching", userId);
        Map<String, Object> stats = gemService.getUserTransactionStatistics(userId);
        return ResponseEntity.ok(stats);
    }
}
