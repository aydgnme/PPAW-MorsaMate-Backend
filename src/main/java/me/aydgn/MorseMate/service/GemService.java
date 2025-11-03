package me.aydgn.MorseMate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.dto.response.GemTransactionResponse;
import me.aydgn.MorseMate.dto.response.UserGemsResponse;
import me.aydgn.MorseMate.entity.GemTransaction;
import me.aydgn.MorseMate.entity.User;
import me.aydgn.MorseMate.entity.UserGems;
import me.aydgn.MorseMate.exception.InvalidOperationException;
import me.aydgn.MorseMate.exception.ResourceNotFoundException;
import me.aydgn.MorseMate.repository.GemTransactionRepository;
import me.aydgn.MorseMate.repository.UserGemsRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GemService {

    private final UserGemsRepository userGemsRepository;
    private final GemTransactionRepository gemTransactionRepository;
    private final UserService userService;

    /**
     * Get or create user gems record
     */
    @Transactional
    public UserGems getOrCreateUserGems(Long userId) {
        log.debug("Getting or creating gems record for user id: {}", userId);

        User user = userService.getUserById(userId);

        return userGemsRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserGems newUserGems = UserGems.builder()
                            .user(user)
                            .balance(0)
                            .totalEarned(0)
                            .totalSpent(0)
                            .build();
                    UserGems saved = userGemsRepository.save(newUserGems);
                    log.info("Created new gems record for user id: {} with id: {}", userId, saved.getId());
                    return saved;
                });
    }

    /**
     * Get user's gem balance
     */
    @Transactional(readOnly = true)
    public UserGemsResponse getUserGems(Long userId) {
        log.debug("Fetching gems for user id: {}", userId);

        UserGems userGems = getOrCreateUserGems(userId);
        return UserGemsResponse.from(userGems);
    }

    /**
     * Add gems to user (earn)
     */
    @Transactional
    public UserGemsResponse addGems(Long userId, Integer amount, String source, String description) {
        log.debug("Adding {} gems to user id: {} from source: {}", amount, userId, source);

        if (amount == null || amount <= 0) {
            throw new InvalidOperationException("Amount must be greater than 0");
        }

        UserGems userGems = getOrCreateUserGems(userId);

        // Update balance
        userGems.earn(amount);
        userGems = userGemsRepository.save(userGems);

        // Record transaction
        GemTransaction transaction = GemTransaction.builder()
                .user(userGems.getUser())
                .amount(amount)
                .transactionType(GemTransaction.Type.EARN)
                .source(source)
                .description(description)
                .build();

        gemTransactionRepository.save(transaction);

        log.info("Added {} gems to user id: {}. New balance: {}", amount, userId, userGems.getBalance());

        return UserGemsResponse.from(userGems);
    }

    /**
     * Spend gems from user
     */
    @Transactional
    public UserGemsResponse spendGems(Long userId, Integer amount, String source, String description) {
        log.debug("Spending {} gems from user id: {} for: {}", amount, userId, source);

        if (amount == null || amount <= 0) {
            throw new InvalidOperationException("Amount must be greater than 0");
        }

        UserGems userGems = getOrCreateUserGems(userId);

        // Check if user has enough gems
        if (userGems.getBalance() < amount) {
            log.error("User id: {} has insufficient gems. Balance: {}, Required: {}",
                      userId, userGems.getBalance(), amount);
            throw new InvalidOperationException(
                    String.format("Insufficient gems. You have %d gems but need %d",
                                  userGems.getBalance(), amount)
            );
        }

        // Update balance
        boolean success = userGems.spend(amount);
        if (!success) {
            throw new InvalidOperationException("Failed to spend gems");
        }

        userGems = userGemsRepository.save(userGems);

        // Record transaction (negative amount for spending)
        GemTransaction transaction = GemTransaction.builder()
                .user(userGems.getUser())
                .amount(-amount)
                .transactionType(GemTransaction.Type.SPEND)
                .source(source)
                .description(description)
                .build();

        gemTransactionRepository.save(transaction);

        log.info("Spent {} gems from user id: {}. New balance: {}", amount, userId, userGems.getBalance());

        return UserGemsResponse.from(userGems);
    }

    /**
     * Purchase gems (for future premium integration)
     */
    @Transactional
    public UserGemsResponse purchaseGems(Long userId, Integer amount, String purchaseId) {
        log.debug("Processing gem purchase for user id: {}. Amount: {}", userId, amount);

        if (amount == null || amount <= 0) {
            throw new InvalidOperationException("Amount must be greater than 0");
        }

        UserGems userGems = getOrCreateUserGems(userId);

        // Update balance
        userGems.earn(amount);
        userGems = userGemsRepository.save(userGems);

        // Record transaction
        GemTransaction transaction = GemTransaction.builder()
                .user(userGems.getUser())
                .amount(amount)
                .transactionType(GemTransaction.Type.PURCHASE)
                .source("Purchase: " + purchaseId)
                .description("Purchased " + amount + " gems")
                .build();

        gemTransactionRepository.save(transaction);

        log.info("User id: {} purchased {} gems. New balance: {}", userId, amount, userGems.getBalance());

        return UserGemsResponse.from(userGems);
    }

    /**
     * Award bonus gems
     */
    @Transactional
    public UserGemsResponse awardBonusGems(Long userId, Integer amount, String reason) {
        log.debug("Awarding {} bonus gems to user id: {}. Reason: {}", amount, userId, reason);

        if (amount == null || amount <= 0) {
            throw new InvalidOperationException("Amount must be greater than 0");
        }

        UserGems userGems = getOrCreateUserGems(userId);

        // Update balance
        userGems.earn(amount);
        userGems = userGemsRepository.save(userGems);

        // Record transaction
        GemTransaction transaction = GemTransaction.builder()
                .user(userGems.getUser())
                .amount(amount)
                .transactionType(GemTransaction.Type.BONUS)
                .source("Bonus")
                .description(reason)
                .build();

        gemTransactionRepository.save(transaction);

        log.info("Awarded {} bonus gems to user id: {}. New balance: {}", amount, userId, userGems.getBalance());

        return UserGemsResponse.from(userGems);
    }

    /**
     * Get user's gem transactions
     */
    @Transactional(readOnly = true)
    public List<GemTransactionResponse> getUserTransactions(Long userId) {
        log.debug("Fetching gem transactions for user id: {}", userId);

        User user = userService.getUserById(userId);

        List<GemTransaction> transactions = gemTransactionRepository.findByUserOrderByCreatedAtDesc(user);

        return transactions.stream()
                .map(GemTransactionResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Get user's gem transactions with pagination
     */
    @Transactional(readOnly = true)
    public Page<GemTransactionResponse> getUserTransactionsPaged(Long userId, Pageable pageable) {
        log.debug("Fetching paged gem transactions for user id: {}", userId);

        User user = userService.getUserById(userId);

        Page<GemTransaction> transactions = gemTransactionRepository.findByUser(user, pageable);

        return transactions.map(GemTransactionResponse::from);
    }

    /**
     * Get user's gem transactions by type
     */
    @Transactional(readOnly = true)
    public List<GemTransactionResponse> getUserTransactionsByType(Long userId, GemTransaction.Type type) {
        log.debug("Fetching {} transactions for user id: {}", type, userId);

        User user = userService.getUserById(userId);

        List<GemTransaction> transactions = gemTransactionRepository
                .findByUserAndTransactionTypeOrderByCreatedAtDesc(user, type);

        return transactions.stream()
                .map(GemTransactionResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Get user's gem transactions in time range
     */
    @Transactional(readOnly = true)
    public List<GemTransactionResponse> getUserTransactionsInRange(Long userId, LocalDateTime from, LocalDateTime to) {
        log.debug("Fetching gem transactions for user id: {} from {} to {}", userId, from, to);

        User user = userService.getUserById(userId);

        List<GemTransaction> transactions = gemTransactionRepository
                .findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(user, from, to);

        return transactions.stream()
                .map(GemTransactionResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Get transaction statistics for user
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getUserTransactionStatistics(Long userId) {
        log.debug("Calculating gem transaction statistics for user id: {}", userId);

        User user = userService.getUserById(userId);
        UserGems userGems = getOrCreateUserGems(userId);

        long totalTransactions = gemTransactionRepository.countByUser(user);
        Integer netAmount = gemTransactionRepository.netAmountForUser(user);

        Map<String, Object> stats = new HashMap<>();
        stats.put("currentBalance", userGems.getBalance());
        stats.put("totalEarned", userGems.getTotalEarned());
        stats.put("totalSpent", userGems.getTotalSpent());
        stats.put("netAmount", netAmount != null ? netAmount : 0);
        stats.put("totalTransactions", totalTransactions);
        stats.put("lastUpdated", userGems.getLastUpdated());

        return stats;
    }

    /**
     * Get net gem amount for user in time range
     */
    @Transactional(readOnly = true)
    public Integer getNetAmountInRange(Long userId, LocalDateTime from, LocalDateTime to) {
        log.debug("Calculating net gem amount for user id: {} from {} to {}", userId, from, to);

        User user = userService.getUserById(userId);

        Integer netAmount = gemTransactionRepository.netAmountForUserInRange(user, from, to);
        return netAmount != null ? netAmount : 0;
    }

    /**
     * Check if user has enough gems
     */
    @Transactional(readOnly = true)
    public boolean hasEnoughGems(Long userId, Integer amount) {
        if (amount == null || amount <= 0) {
            return false;
        }

        UserGems userGems = getOrCreateUserGems(userId);
        return userGems.getBalance() >= amount;
    }

    /**
     * Get transaction count for user
     */
    @Transactional(readOnly = true)
    public long getUserTransactionCount(Long userId) {
        log.debug("Counting gem transactions for user id: {}", userId);

        User user = userService.getUserById(userId);
        return gemTransactionRepository.countByUser(user);
    }
}
