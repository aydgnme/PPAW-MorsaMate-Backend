package me.aydgn.MorseMate.repository;

import me.aydgn.MorseMate.entity.User;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    // --- Lookups (case-insensitive for login) ---
    Optional<User> findByEmailIgnoreCase(String email);
    Optional<User> findByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);
    boolean existsByUsernameIgnoreCase(String username);

    // --- Auth/usage bookkeeping ---
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("update User u set u.lastLogin = :ts where u.id = :id")
    int touchLastLogin(@Param("id") Long id, @Param("ts") LocalDateTime ts);

    // Add points and recompute level in one atomic step
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
           update User u
              set u.totalPoints = u.totalPoints + :delta,
                  u.level = ( (u.totalPoints + :delta) / 100 ) + 1
            where u.id = :id
           """)
    int addPointsAndRelevel(@Param("id") Long id, @Param("delta") int delta);

    // Spend one heart only if available (returns 1 if success, 0 otherwise)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("update User u set u.hearts = u.hearts - 1 where u.id = :id and u.hearts > 0")
    int useOneHeartIfAvailable(@Param("id") Long id);

    // Refill hearts to max (you may gate this in service with your 1h rule)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
           update User u
              set u.hearts = u.maxHearts,
                  u.lastHeartRefill = :now
            where u.id = :id and u.hearts < u.maxHearts
           """)
    int refillHeartsIfNotFull(@Param("id") Long id, @Param("now") LocalDateTime now);
}