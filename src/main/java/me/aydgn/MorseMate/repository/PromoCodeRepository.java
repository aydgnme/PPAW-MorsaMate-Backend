package me.aydgn.MorseMate.repository;

import me.aydgn.MorseMate.entity.PromoCode;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromoCodeRepository extends JpaRepository<PromoCode, Long>, JpaSpecificationExecutor<PromoCode> {

    Optional<PromoCode> findByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCase(String code);

    List<PromoCode> findAllByIsActiveTrue();

    // Valid codes at a moment in time
    @Query("""
           select p from PromoCode p
            where p.isActive = true
              and (p.validFrom is null or p.validFrom <= :at)
              and (p.validUntil is null or p.validUntil >= :at)
           """)
    List<PromoCode> findAllValidAt(LocalDateTime at);
}