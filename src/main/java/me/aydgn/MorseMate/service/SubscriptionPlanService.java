package me.aydgn.MorseMate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.dto.request.SubscriptionPlanRequest;
import me.aydgn.MorseMate.dto.response.SubscriptionPlanResponse;
import me.aydgn.MorseMate.entity.SubscriptionPlan;
import me.aydgn.MorseMate.exception.DuplicateResourceException;
import me.aydgn.MorseMate.exception.InvalidOperationException;
import me.aydgn.MorseMate.exception.ResourceNotFoundException;
import me.aydgn.MorseMate.repository.SubscriptionPlanRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionPlanService {

    private final SubscriptionPlanRepository subscriptionPlanRepository;

    /**
     * Get all active subscription plans
     */
    @Cacheable(value = "subscription-plans", key = "'active'")
    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponse> getAllActivePlans() {
        log.debug("Fetching all active subscription plans");
        List<SubscriptionPlan> plans = subscriptionPlanRepository.findAllByIsActiveTrue();
        return plans.stream()
                .map(SubscriptionPlanResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Get all subscription plans (including inactive)
     */
    @Cacheable(value = "subscription-plans", key = "'all'")
    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponse> getAllPlans() {
        log.debug("Fetching all subscription plans");
        List<SubscriptionPlan> plans = subscriptionPlanRepository.findAll();
        return plans.stream()
                .map(SubscriptionPlanResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Get subscription plan by ID
     */
    @Cacheable(value = "subscription-plans", key = "#id")
    @Transactional(readOnly = true)
    public SubscriptionPlanResponse getPlanById(Long id) {
        log.debug("Fetching subscription plan with id: {}", id);
        SubscriptionPlan plan = findPlanById(id);
        return SubscriptionPlanResponse.from(plan);
    }

    /**
     * Get subscription plan entity by ID (internal use)
     */
    @Transactional(readOnly = true)
    public SubscriptionPlan findPlanById(Long id) {
        return subscriptionPlanRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Subscription plan not found with id: {}", id);
                    return new ResourceNotFoundException("SubscriptionPlan", "id", id);
                });
    }

    /**
     * Get plans by billing period
     */
    @Cacheable(value = "subscription-plans", key = "'period_' + #billingPeriod")
    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponse> getPlansByBillingPeriod(SubscriptionPlan.BillingPeriod billingPeriod) {
        log.debug("Fetching subscription plans with billing period: {}", billingPeriod);
        List<SubscriptionPlan> plans = subscriptionPlanRepository.findAllByBillingPeriod(billingPeriod);
        return plans.stream()
                .map(SubscriptionPlanResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Create a new subscription plan
     */
    @CacheEvict(value = "subscription-plans", allEntries = true)
    @Transactional
    public SubscriptionPlanResponse createPlan(SubscriptionPlanRequest request) {
        log.debug("Creating new subscription plan with name: {}", request.getName());

        // Validate request
        validatePlanRequest(request);

        // Check if plan with same name already exists
        if (subscriptionPlanRepository.existsByNameIgnoreCase(request.getName())) {
            log.error("Subscription plan already exists with name: {}", request.getName());
            throw new DuplicateResourceException("SubscriptionPlan", "name", request.getName());
        }

        // Create new plan
        SubscriptionPlan plan = SubscriptionPlan.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .billingPeriod(request.getBillingPeriod())
                .features(request.getFeatures())
                .maxHearts(request.getMaxHearts())
                .dailyPracticeLimit(request.getDailyPracticeLimit())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        plan = subscriptionPlanRepository.save(plan);
        log.info("Subscription plan created successfully with id: {} and name: {}", plan.getId(), plan.getName());

        return SubscriptionPlanResponse.from(plan);
    }

    /**
     * Update an existing subscription plan
     */
    @CacheEvict(value = "subscription-plans", allEntries = true)
    @Transactional
    public SubscriptionPlanResponse updatePlan(Long id, SubscriptionPlanRequest request) {
        log.debug("Updating subscription plan with id: {}", id);

        SubscriptionPlan plan = findPlanById(id);

        // Validate request
        validatePlanRequest(request);

        // Check if new name conflicts with existing plan
        if (!request.getName().equalsIgnoreCase(plan.getName()) &&
            subscriptionPlanRepository.existsByNameIgnoreCase(request.getName())) {
            log.error("Subscription plan already exists with name: {}", request.getName());
            throw new DuplicateResourceException("SubscriptionPlan", "name", request.getName());
        }

        // Update fields
        plan.setName(request.getName());
        plan.setDescription(request.getDescription());
        plan.setPrice(request.getPrice());
        plan.setBillingPeriod(request.getBillingPeriod());
        plan.setFeatures(request.getFeatures());
        plan.setMaxHearts(request.getMaxHearts());
        plan.setDailyPracticeLimit(request.getDailyPracticeLimit());

        if (request.getIsActive() != null) {
            plan.setIsActive(request.getIsActive());
        }

        plan = subscriptionPlanRepository.save(plan);
        log.info("Subscription plan updated successfully with id: {}", plan.getId());

        return SubscriptionPlanResponse.from(plan);
    }

    /**
     * Delete a subscription plan (soft delete)
     */
    @CacheEvict(value = "subscription-plans", allEntries = true)
    @Transactional
    public void deletePlan(Long id) {
        log.debug("Deleting subscription plan with id: {}", id);

        SubscriptionPlan plan = findPlanById(id);

        // TODO: Check if plan has active subscriptions
        // For now, we just set isActive to false

        plan.setIsActive(false);
        subscriptionPlanRepository.save(plan);

        log.info("Subscription plan soft deleted with id: {}", id);
    }

    /**
     * Validate subscription plan request
     */
    private void validatePlanRequest(SubscriptionPlanRequest request) {
        // Validate price
        if (request.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            log.error("Price must be greater than or equal to 0");
            throw new InvalidOperationException("Price must be greater than or equal to 0");
        }

        // Validate max hearts
        if (request.getMaxHearts() < 1 || request.getMaxHearts() > 100) {
            log.error("Max hearts must be between 1 and 100");
            throw new InvalidOperationException("Max hearts must be between 1 and 100");
        }

        // Validate daily practice limit if provided
        if (request.getDailyPracticeLimit() != null) {
            if (request.getDailyPracticeLimit() < 1 || request.getDailyPracticeLimit() > 1000) {
                log.error("Daily practice limit must be between 1 and 1000");
                throw new InvalidOperationException("Daily practice limit must be between 1 and 1000");
            }
        }

        // Validate billing period
        if (request.getBillingPeriod() == null) {
            log.error("Billing period is required");
            throw new InvalidOperationException("Billing period is required");
        }
    }

    /**
     * Check if at least one plan is active
     */
    @Transactional(readOnly = true)
    public boolean hasActivePlans() {
        long count = subscriptionPlanRepository.findAllByIsActiveTrue().size();
        return count > 0;
    }
}
