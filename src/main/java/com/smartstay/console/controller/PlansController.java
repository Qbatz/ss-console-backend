package com.smartstay.console.controller;

import com.smartstay.console.payloads.plans.PlanFeaturesPayload;
import com.smartstay.console.payloads.plans.PlansPayload;
import com.smartstay.console.payloads.plans.PlansUpdatePayload;
import com.smartstay.console.services.PlansService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v2/plans")
@CrossOrigin(origins = "*")
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "Authorization")
public class PlansController {

    @Autowired
    private PlansService plansService;

    @GetMapping
    private ResponseEntity<?> getAllPlans(){
        return plansService.getAllPlans();
    }

    @PutMapping("/{planId}")
    private ResponseEntity<?> updatePlanByPlanId(@PathVariable("planId") Long planId,
                                                 @RequestBody @Valid PlansUpdatePayload payload){
        return plansService.updatePlanByPlanId(planId, payload);
    }

    @PostMapping
    private ResponseEntity<?> addPlan(@RequestBody @Valid PlansPayload payload){
        return plansService.addPlan(payload);
    }

    @PutMapping("/deactivate-plan/{planId}")
    private ResponseEntity<?> deactivatePlan(@PathVariable("planId") Long planId){
        return plansService.deactivatePlan(planId);
    }

    @PostMapping("/plan-feature/{planId}")
    private ResponseEntity<?> addPlanFeature(@PathVariable("planId") Long planId,
                                             @RequestBody @Valid PlanFeaturesPayload payload){
        return plansService.addPlanFeature(planId, payload);
    }

    @PutMapping("/plan-feature/{planFeatureId}")
    private ResponseEntity<?> deactivatePlanFeature(@PathVariable("planFeatureId") Long planFeatureId){
        return plansService.deactivatePlanFeature(planFeatureId);
    }

//    @GetMapping("/plan-type")
//    public ResponseEntity<?> getPlanType() {
//        return plansService.getPlanType();
//    }
}
