package me.aydgn.MorseMate.repository;

import me.aydgn.MorseMate.entity.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long>, JpaSpecificationExecutor<SubscriptionPlan> {

    Optional<SubscriptionPlan> findByNameIgnoreCase(String name);

    List<SubscriptionPlan> findAllByIsActiveTrue();

    List<SubscriptionPlan> findAllByBillingPeriod(SubscriptionPlan.BillingPeriod billingPeriod);

    boolean existsByNameIgnoreCase(String name);
}