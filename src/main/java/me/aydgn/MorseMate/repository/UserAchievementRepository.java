package me.aydgn.MorseMate.repository;

import me.aydgn.MorseMate.entity.UserAchievement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long>, JpaSpecificationExecutor<UserAchievement> {

    // Unique pair
    Optional<UserAchievement> findByUserIdAndAchievementId(Long userId, Long achievementId);
    boolean existsByUserIdAndAchievementId(Long userId, Long achievementId);

    // Listings
    List<UserAchievement> findByUserIdOrderByEarnedAtDesc(Long userId);
    Page<UserAchievement> findByUserId(Long userId, Pageable pageable);
    List<UserAchievement> findByAchievementId(Long achievementId);

    long countByUserId(Long userId);
    long countByAchievementId(Long achievementId);

    // Range
    List<UserAchievement> findByUserIdAndEarnedAtBetweenOrderByEarnedAtDesc(
            Long userId, LocalDateTime from, LocalDateTime to
    );

    // Simple leaderboard: how many users earned a specific achievement
    @Query("""
           select count(ua)
             from UserAchievement ua
            where ua.achievement.id = :achievementId
           """)
    long totalEarners(@Param("achievementId") Long achievementId);
}