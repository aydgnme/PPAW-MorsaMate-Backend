package me.aydgn.MorseMate.repository;

import me.aydgn.MorseMate.entity.UserPowerUp;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserPowerUpRepository extends JpaRepository<UserPowerUp, Long>, JpaSpecificationExecutor<UserPowerUp> {

    // Listings
    List<UserPowerUp> findByUserIdOrderByPurchasedAtDesc(Long userId);
    Page<UserPowerUp> findByUserId(Long userId, Pageable pageable);

    List<UserPowerUp> findByUserIdAndIsActiveTrue(Long userId);

    // Active window queries
    @Query("""
           select up from UserPowerUp up
            where up.user.id = :userId
              and up.isActive = true
              and (up.expiresAt is null or up.expiresAt > :now)
            order by up.purchasedAt desc
           """)
    List<UserPowerUp> findCurrentlyActive(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    // Expired but still flagged active (cleanup candidates)
    @Query("""
           select up from UserPowerUp up
            where up.isActive = true
              and up.expiresAt is not null
              and up.expiresAt <= :now
           """)
    List<UserPowerUp> findExpiredActives(@Param("now") LocalDateTime now);
}