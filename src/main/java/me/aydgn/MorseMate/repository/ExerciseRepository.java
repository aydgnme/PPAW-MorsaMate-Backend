package me.aydgn.MorseMate.repository;

import me.aydgn.MorseMate.entity.Exercise;
import me.aydgn.MorseMate.entity.Exercise.Difficulty;
import me.aydgn.MorseMate.entity.Exercise.Type;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Long>, JpaSpecificationExecutor<Exercise> {

    List<Exercise> findByLessonId(Long lessonId);

    Page<Exercise> findByLessonId(Long lessonId, Pageable pageable);

    List<Exercise> findByLessonIdAndType(Long lessonId, Type type);

    Page<Exercise> findByLessonIdAndType(Long lessonId, Type type, Pageable pageable);

    List<Exercise> findByLessonIdAndDifficulty(Long lessonId, Difficulty difficulty);

    Page<Exercise> findByLessonIdAndDifficulty(Long lessonId, Difficulty difficulty, Pageable pageable);

    Page<Exercise> findByQuestionContainingIgnoreCase(String question, Pageable pageable);

    @EntityGraph(attributePaths = "lesson")
    Optional<Exercise> findWithLessonById(Long lessonId);

    long countByLessonId(Long lessonId);

    long countByLessonIdAndType(Long lessonId, Type type);

    long countByLessonIdAndDifficulty(Long lessonId, Difficulty difficulty);

    @Query(value = """
            select e from Exercise e
             where e.lesson.id = :lessonId
               and (:type is null or e.type = :type)
               and (:difficulty is null or e.difficulty = :difficulty)
             order by function('random')
            """)
    Page<Exercise> findRandomByLesson(
            @Param("lessonId") Long lessonId,
            @Param("type") Type type,
            @Param("difficulty") Difficulty difficulty,
            Pageable pageable
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Exercise e set e.points = :points where e.id = :id")
    int updatePoints(@Param("id") Long id, @Param("points") int points);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Exercise e set e.timeLimit = :sec where e.id = :id")
    int updateTimeLimit(@Param("id") Long id, @Param("sec") Integer seconds);
}
