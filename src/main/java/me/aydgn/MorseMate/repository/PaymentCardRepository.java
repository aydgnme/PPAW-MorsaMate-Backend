package me.aydgn.MorseMate.repository;

import me.aydgn.MorseMate.entity.PaymentCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long> {

    List<PaymentCard> findByUserIdOrderByIsDefaultDescCreatedAtDesc(Long userId);

    Optional<PaymentCard> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndIsDefaultTrue(Long userId);
}


