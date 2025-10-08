package me.aydgn.MorseMate.repository;

import me.aydgn.MorseMate.entity.UserGems;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserGemsRepository extends JpaRepository<UserGems, Long>, JpaSpecificationExecutor<UserGems> {

    // Lookups
    Optional<UserGems> findByUserId(Long userId);
    boolean existsByUserId(Long userId);

    // --- Atomic balance updates ---

    /** Earn gems (adds to balance and totalEarned). Returns #rows updated (0/1). */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
           update UserGems g
              set g.balance = g.balance + :amount,
                  g.totalEarned = g.totalEarned + :amount,
                  g.lastUpdated = CURRENT_TIMESTAMP
            where g.user.id = :userId
           """)
    int earn(@Param("userId") Long userId, @Param("amount") int amount);

    /** Spend gems only if enough balance. Returns 1 on success, 0 if insufficient. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
           update UserGems g
              set g.balance = g.balance - :amount,
                  g.totalSpent = g.totalSpent + :amount,
                  g.lastUpdated = CURRENT_TIMESTAMP
            where g.user.id = :userId and g.balance >= :amount
           """)
    int spendIfEnough(@Param("userId") Long userId, @Param("amount") int amount);

    /** Set exact balance (use with caution; primarily for admin/tools). */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
           update UserGems g
              set g.balance = :newBalance,
                  g.lastUpdated = CURRENT_TIMESTAMP
            where g.user.id = :userId
           """)
    int setBalance(@Param("userId") Long userId, @Param("newBalance") int newBalance);
}