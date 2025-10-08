package me.aydgn.MorseMate.repository;

import me.aydgn.MorseMate.entity.UserProgress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserProgressRepository extends JpaRepository<UserProgress, Long>, JpaSpecificationExecutor<UserProgress> {

    // --- Uniqueness pair (user_id, lesson_id) ---
    Optional<UserProgress> findByUserIdAndLessonId(Long userId, Long lessonId);
    boolean existsByUserIdAndLessonId(Long userId, Long lessonId);

    // --- Listings ---
    List<UserProgress> findByUserIdOrderByCompletedAtDesc(Long userId);
    Page<UserProgress> findByUserId(Long userId, Pageable pageable);
    List<UserProgress> findByLessonId(Long lessonId);

    // --- Stats / aggregates ---
    long countByUserIdAndIsCompletedTrue(Long userId);
    long countByLessonIdAndIsCompletedTrue(Long lessonId);

    @Query("""
           select coalesce(avg(up.score), 0)
             from UserProgress up
            where up.lesson.id = :lessonId
              and up.isCompleted = true
           """)
    Double averageScoreForLesson(@Param("lessonId") Long lessonId);

    // --- Atomic updates ---
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("update UserProgress up set up.attempts = up.attempts + 1 where up.id = :id")
    int incrementAttempts(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
           update UserProgress up
              set up.isCompleted  = true,
                  up.completedAt  = :completedAt,
                  up.score        = :score,
                  up.starsEarned  = :stars,
                  up.timeSpent    = :timeSpent
            where up.user.id = :userId and up.lesson.id = :lessonId
           """)
    int markCompleted(@Param("userId") Long userId,
                      @Param("lessonId") Long lessonId,
                      @Param("completedAt") LocalDateTime completedAt,
                      @Param("score") Integer score,
                      @Param("stars") Integer stars,
                      @Param("timeSpent") Integer timeSpent);

    // Optionally clear completion (for retries/resets)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
           update UserProgress up
              set up.isCompleted = false,
                  up.completedAt = null,
                  up.score       = null,
                  up.starsEarned = null,
                  up.timeSpent   = null
            where up.user.id = :userId and up.lesson.id = :lessonId
           """)
    int resetCompletion(@Param("userId") Long userId, @Param("lessonId") Long lessonId);
}