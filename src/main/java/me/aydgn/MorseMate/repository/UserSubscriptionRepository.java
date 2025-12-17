package me.aydgn.MorseMate.repository;

import me.aydgn.MorseMate.entity.User;
import me.aydgn.MorseMate.entity.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {

    Optional<UserSubscription> findByStripeSubscriptionId(String stripeSubscriptionId);

    Optional<UserSubscription> findByUserAndStatus(User user, UserSubscription.SubscriptionStatus status);

    @Query("SELECT us FROM UserSubscription us WHERE us.user.id = :userId")
    Optional<UserSubscription> findByUserId(@Param("userId") Long userId);

        @Query("SELECT CASE WHEN COUNT(us) > 0 THEN TRUE ELSE FALSE END " +
            "FROM UserSubscription us, SubscriptionPlan sp " +
            "WHERE us.user.id = :userId AND us.status = 'ACTIVE' " +
            "AND sp.id = us.planId AND sp.isActive = TRUE AND sp.price > 0")
        boolean isUserPremium(@Param("userId") Long userId);

    List<UserSubscription> findByStatus(UserSubscription.SubscriptionStatus status);
}
