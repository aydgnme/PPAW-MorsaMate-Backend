package me.aydgn.MorseMate.repository;

import jakarta.persistence.Entity;
import me.aydgn.MorseMate.entity.Category;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {

    Optional<Category> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);

    List<Category> findAllByOrderByNameAsc();

    @EntityGraph(attributePaths = "lessons")
    Optional<Category> findWithLessonsById(Long lessonId);

    @EntityGraph(attributePaths = "lessons")
    List<Category> findAllByOrderByOrderIndexAsc();

    @Query("select count(1) from Lesson l where l.category.id = :categoryId")
    long countLessons(@Param("categoryId") Long categoryId);
}
