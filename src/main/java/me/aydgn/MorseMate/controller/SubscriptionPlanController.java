package me.aydgn.MorseMate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.dto.request.SubscriptionPlanRequest;
import me.aydgn.MorseMate.dto.response.ApiMessage;
import me.aydgn.MorseMate.dto.response.SubscriptionPlanResponse;
import me.aydgn.MorseMate.entity.SubscriptionPlan;
import me.aydgn.MorseMate.service.SubscriptionPlanService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Subscription Plan management
 * Provides endpoints for CRUD operations on subscription plans
 *
 * Endpoints:
 * - GET    /v1/subscriptions/plans           - Get all active plans (Public)
 * - GET    /v1/subscriptions/plans/{id}      - Get plan by ID (Public)
 * - POST   /v1/subscriptions/plans           - Create new plan (Admin)
 * - PUT    /v1/subscriptions/plans/{id}      - Update plan (Admin)
 * - DELETE /v1/subscriptions/plans/{id}      - Delete plan (Admin)
 * - GET    /v1/subscriptions/plans/admin/all - Get all plans including inactive (Admin)
 * - GET    /v1/subscriptions/plans/period/{period} - Get plans by billing period (Public)
 */
@RestController
@RequestMapping("/${api.version}/subscriptions/plans")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionPlanController {

    private final SubscriptionPlanService subscriptionPlanService;

    /**
     * GET /v1/subscriptions/plans
     * Get all active subscription plans
     * Public endpoint - no authentication required
     *
     * @return List of active subscription plans
     */
    @GetMapping
    public ResponseEntity<List<SubscriptionPlanResponse>> getAllActivePlans() {
        log.debug("GET /v1/subscriptions/plans - Fetching all active plans");
        List<SubscriptionPlanResponse> plans = subscriptionPlanService.getAllActivePlans();
        return ResponseEntity.ok(plans);
    }

    /**
     * GET /v1/subscriptions/plans/{id}
     * Get a single subscription plan by ID
     * Public endpoint - no authentication required
     *
     * @param id Plan ID
     * @return Subscription plan details
     */
    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionPlanResponse> getPlanById(@PathVariable("id") Long id) {
        log.debug("GET /v1/subscriptions/plans/{} - Fetching plan", id);
        SubscriptionPlanResponse plan = subscriptionPlanService.getPlanById(id);
        return ResponseEntity.ok(plan);
    }

    /**
     * GET /v1/subscriptions/plans/period/{period}
     * Get subscription plans by billing period
     * Public endpoint - no authentication required
     *
     * @param period Billing period (MONTHLY, YEARLY, LIFETIME)
     * @return List of plans with specified billing period
     */
    @GetMapping("/period/{period}")
    public ResponseEntity<List<SubscriptionPlanResponse>> getPlansByBillingPeriod(
            @PathVariable("period") SubscriptionPlan.BillingPeriod period) {
        log.debug("GET /v1/subscriptions/plans/period/{} - Fetching plans by billing period", period);
        List<SubscriptionPlanResponse> plans = subscriptionPlanService.getPlansByBillingPeriod(period);
        return ResponseEntity.ok(plans);
    }

    /**
     * POST /v1/subscriptions/plans
     * Create a new subscription plan
     * Admin only endpoint
     *
     * @param request Subscription plan creation request
     * @return Created subscription plan
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubscriptionPlanResponse> createPlan(
            @Valid @RequestBody SubscriptionPlanRequest request) {
        log.info("POST /v1/subscriptions/plans - Creating new plan: {}", request.getName());
        SubscriptionPlanResponse created = subscriptionPlanService.createPlan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /v1/subscriptions/plans/{id}
     * Update an existing subscription plan
     * Admin only endpoint
     *
     * @param id Plan ID
     * @param request Subscription plan update request
     * @return Updated subscription plan
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubscriptionPlanResponse> updatePlan(
            @PathVariable("id") Long id,
            @Valid @RequestBody SubscriptionPlanRequest request) {
        log.info("PUT /v1/subscriptions/plans/{} - Updating plan", id);
        SubscriptionPlanResponse updated = subscriptionPlanService.updatePlan(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /v1/subscriptions/plans/{id}
     * Delete a subscription plan (soft delete)
     * Admin only endpoint
     *
     * @param id Plan ID
     * @return Success message
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiMessage> deletePlan(@PathVariable("id") Long id) {
        log.info("DELETE /v1/subscriptions/plans/{} - Soft deleting plan", id);
        subscriptionPlanService.deletePlan(id);
        return ResponseEntity.ok(new ApiMessage("Subscription plan deleted successfully"));
    }

    /**
     * GET /v1/subscriptions/plans/admin/all
     * Get all subscription plans (including inactive)
     * Admin only endpoint
     *
     * @return List of all subscription plans
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SubscriptionPlanResponse>> getAllPlans() {
        log.debug("GET /v1/subscriptions/plans/admin/all - Fetching all plans (including inactive)");
        List<SubscriptionPlanResponse> plans = subscriptionPlanService.getAllPlans();
        return ResponseEntity.ok(plans);
    }
}
