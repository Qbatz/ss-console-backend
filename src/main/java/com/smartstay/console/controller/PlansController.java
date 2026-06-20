package com.smartstay.console.controller;

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

    @GetMapping("/dropdown")
    private ResponseEntity<?> getPlansDropdown(){
        return plansService.getPlansDropdown();
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

    @PutMapping("/reactivate-plan/{planId}")
    private ResponseEntity<?> reactivatePlan(@PathVariable("planId") Long planId){
        return plansService.reactivatePlan(planId);
    }

    @GetMapping("/smartstay-feature")
    private ResponseEntity<?> getSmartstayFeatures(){
        return plansService.getSmartstayFeatures();
    }
}
