package me.aydgn.MorseMate.repository;

import me.aydgn.MorseMate.entity.ExerciseAttempt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExerciseAttemptRepository extends JpaRepository<ExerciseAttempt, Long>, JpaSpecificationExecutor<ExerciseAttempt> {

    // --- Basics / listings ---
    List<ExerciseAttempt> findByUserIdOrderByAttemptedAtDesc(Long userId);
    Page<ExerciseAttempt> findByUserId(Long userId, Pageable pageable);

    List<ExerciseAttempt> findByExerciseIdOrderByAttemptedAtDesc(Long exerciseId);
    Page<ExerciseAttempt> findByExerciseId(Long exerciseId, Pageable pageable);

    // User + Exercise scoped
    List<ExerciseAttempt> findByUserIdAndExerciseIdOrderByAttemptedAtDesc(Long userId, Long exerciseId);
    Page<ExerciseAttempt> findByUserIdAndExerciseId(Long userId, Long exerciseId, Pageable pageable);

    // Last attempt (quick check)
    Optional<ExerciseAttempt> findFirstByUserIdAndExerciseIdOrderByAttemptedAtDesc(Long userId, Long exerciseId);

    // --- Time-window queries ---
    List<ExerciseAttempt> findByUserIdAndAttemptedAtBetweenOrderByAttemptedAtDesc(Long userId, LocalDateTime from, LocalDateTime to);
    Page<ExerciseAttempt> findByUserIdAndAttemptedAtBetween(Long userId, LocalDateTime from, LocalDateTime to, Pageable pageable);

    // --- Stats ---
    long countByUserId(Long userId);
    long countByUserIdAndIsCorrectTrue(Long userId);
    long countByUserIdAndExerciseId(Long userId, Long exerciseId);
    long countByUserIdAndExerciseIdAndIsCorrectTrue(Long userId, Long exerciseId);

    @Query("""
           select coalesce(avg(ea.timeTaken), 0)
             from ExerciseAttempt ea
            where ea.user.id = :userId
              and ea.exercise.id = :exerciseId
           """)
    Double averageTimeForUserAndExercise(@Param("userId") Long userId, @Param("exerciseId") Long exerciseId);

    // Correctness rate over a time range (0..1)
    @Query("""
           select case when count(ea) = 0 then 0.0
                       else sum(case when ea.isCorrect = true then 1 else 0 end) * 1.0 / count(ea)
                  end
             from ExerciseAttempt ea
            where ea.user.id = :userId
              and ea.attemptedAt between :from and :to
           """)
    Double correctnessRateInRange(@Param("userId") Long userId,
                                  @Param("from") LocalDateTime from,
                                  @Param("to") LocalDateTime to);

    // --- Leaderboard-ish aggregations (examples) ---
    @Query("""
           select ea.user.id as userId, count(ea) as attempts
             from ExerciseAttempt ea
            where ea.exercise.id = :exerciseId
            group by ea.user.id
            order by attempts desc
           """)
    List<Object[]> attemptsPerUserForExercise(@Param("exerciseId") Long exerciseId);

    @Query("""
           select ea.user.id as userId,
                  sum(case when ea.isCorrect = true then 1 else 0 end) as correctCount
             from ExerciseAttempt ea
            where ea.exercise.id = :exerciseId
            group by ea.user.id
            order by correctCount desc
           """)
    List<Object[]> correctPerUserForExercise(@Param("exerciseId") Long exerciseId);
}