package me.aydgn.MorseMate.repository;

import me.aydgn.MorseMate.entity.UserSubscription;
import me.aydgn.MorseMate.entity.UserSubscription.Status;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long>, JpaSpecificationExecutor<UserSubscription> {

    Optional<UserSubscription> findByUserId(Long userId);
    boolean existsByUserId(Long userId);

    List<UserSubscription> findByStatus(Status status);

    @EntityGraph(attributePaths = {"plan", "user"})
    Optional<UserSubscription> findWithPlanByUserId(Long userId);

    // Active subscription check
    @Query("""
           select case when count(us) > 0 then true else false end
             from UserSubscription us
            where us.user.id = :userId
              and us.status = 'ACTIVE'
              and (us.endDate is null or us.endDate > CURRENT_TIMESTAMP)
           """)
    boolean isUserPremium(@Param("userId") Long userId);

    // Deactivate subscription
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
           update UserSubscription us
              set us.status = 'CANCELLED',
                  us.autoRenew = false,
                  us.updatedAt = :now
            where us.user.id = :userId
           """)
    int cancelSubscription(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    // Extend subscription period (for manual renewal or promotion)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
           update UserSubscription us
              set us.endDate = :newEnd,
                  us.nextBillingDate = :nextBilling,
                  us.updatedAt = :now
            where us.user.id = :userId
              and us.status = 'ACTIVE'
           """)
    int extendSubscription(@Param("userId") Long userId,
                           @Param("newEnd") LocalDateTime newEnd,
                           @Param("nextBilling") LocalDateTime nextBilling,
                           @Param("now") LocalDateTime now);
}