package me.aydgn.MorseMate.repository;

import me.aydgn.MorseMate.entity.PowerUp;
import me.aydgn.MorseMate.entity.PowerUp.Type;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PowerUpRepository extends JpaRepository<PowerUp, Long>, JpaSpecificationExecutor<PowerUp> {

    Optional<PowerUp> findByNameIgnoreCase(String name);

    List<PowerUp> findAllByIsActiveTrue();

    List<PowerUp> findAllByType(Type type);

    boolean existsByNameIgnoreCase(String name);
}