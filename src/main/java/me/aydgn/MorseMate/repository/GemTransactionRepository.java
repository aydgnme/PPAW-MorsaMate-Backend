package me.aydgn.MorseMate.repository;

import me.aydgn.MorseMate.entity.GemTransaction;
import me.aydgn.MorseMate.entity.GemTransaction.Type;
import me.aydgn.MorseMate.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GemTransactionRepository extends JpaRepository<GemTransaction, Long>, JpaSpecificationExecutor<GemTransaction> {

    // Listings
    List<GemTransaction> findByUserOrderByCreatedAtDesc(User user);
    Page<GemTransaction> findByUser(User user, Pageable pageable);

    List<GemTransaction> findByUserAndTransactionTypeOrderByCreatedAtDesc(User user, Type type);
    Page<GemTransaction> findByUserAndTransactionType(User user, Type type, Pageable pageable);

    List<GemTransaction> findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(User user, LocalDateTime from, LocalDateTime to);
    Page<GemTransaction> findByUserAndCreatedAtBetween(User user, LocalDateTime from, LocalDateTime to, Pageable pageable);

    // Aggregates
    long countByUser(User user);

    @Query("""
           select coalesce(sum(gt.amount), 0)
             from GemTransaction gt
            where gt.user = :user
           """)
    Integer netAmountForUser(@Param("user") User user); // earn - spend etc.

    @Query("""
           select coalesce(sum(gt.amount), 0)
             from GemTransaction gt
            where gt.user = :user
              and gt.createdAt between :from and :to
           """)
    Integer netAmountForUserInRange(@Param("user") User user,
                                    @Param("from") LocalDateTime from,
                                    @Param("to") LocalDateTime to);
}