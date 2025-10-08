package me.aydgn.MorseMate.repository;

import me.aydgn.MorseMate.entity.Achievement;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long>, JpaSpecificationExecutor<Achievement> {

    Optional<Achievement> findByNameIgnoreCase(String name);

    List<Achievement> findAllByOrderByIdAsc();
    List<Achievement> findAllByOrderByNameAsc();

    boolean existsByNameIgnoreCase(String name);
}