package com.smartstay.console.controller;

import com.smartstay.console.payloads.plans.PlansUpdatePayload;
import com.smartstay.console.services.PlansService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
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

//    @PutMapping("/{planId}")
//    private ResponseEntity<?> updatePlanByPlanId(@PathVariable("planId") Long planId,
//                                                 @RequestBody PlansUpdatePayload payload){
//        return plansService.updatePlanByPlanId(planId, payload);
//    }
}
