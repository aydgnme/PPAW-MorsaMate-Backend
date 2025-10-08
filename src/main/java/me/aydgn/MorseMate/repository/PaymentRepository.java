package me.aydgn.MorseMate.repository;

import me.aydgn.MorseMate.entity.Payment;
import me.aydgn.MorseMate.entity.Payment.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long>, JpaSpecificationExecutor<Payment> {

    List<Payment> findByUserIdOrderByTransactionDateDesc(Long userId);
    Page<Payment> findByUserId(Long userId, Pageable pageable);

    List<Payment> findByStatus(Status status);
    long countByStatus(Status status);

    @Query("""
           select sum(p.amount)
             from Payment p
            where p.status = 'COMPLETED'
              and p.transactionDate between :from and :to
           """)
    Double totalRevenueInPeriod(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("""
           select coalesce(avg(p.amount), 0)
             from Payment p
            where p.status = 'COMPLETED'
           """)
    Double averageCompletedPayment();

    // Retrieve user's last completed payment
    @Query("""
           select p from Payment p
            where p.user.id = :userId
              and p.status = 'COMPLETED'
            order by p.transactionDate desc
           """)
    List<Payment> findRecentCompletedPayments(@Param("userId") Long userId, Pageable pageable);
}