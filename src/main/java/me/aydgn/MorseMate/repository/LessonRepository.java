package me.aydgn.MorseMate.repository;

import me.aydgn.MorseMate.entity.Lesson;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long>, JpaSpecificationExecutor<Lesson> {

    List<Lesson> findByCategoryIdOrderByOrderIndexAsc(Long categoryId);

    List<Lesson> findByCategory_NameIgnoreCaseOrderByOrderIndexAsc(String categoryName);

    Page<Lesson> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    @EntityGraph(attributePaths = "exercises")
    Optional<Lesson> findWithExercisesById(Long id);

    @Query("select count(e) from Exercise e where e.lesson.id = :lessonId")
    long countExercises(@Param("lessonId") Long lessonId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Lesson l set l.orderIndex = :orderIndex where l.id = :lessonId")
    int setOrderIndex(@Param("lessonId") Long lessonId, @Param("orderIndex") int orderIndex);
}
