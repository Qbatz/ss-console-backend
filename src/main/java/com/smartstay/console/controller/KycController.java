package com.smartstay.console.controller;

import com.smartstay.console.services.KycDetailsService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v2/kyc")
@CrossOrigin(origins = "*")
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "Authorization")
public class KycController {

    @Autowired
    private KycDetailsService kycDetailsService;

    @GetMapping
    public ResponseEntity<?> getWaitingApprovalKycDetails(@RequestParam(value = "page", defaultValue = "0") int page,
                                                          @RequestParam(value = "size", defaultValue = "10") int size,
                                                          @RequestParam(value = "name", required = false) String name) {
        return kycDetailsService.getWaitingApprovalKycDetails(page, size, name);
    }

    @PostMapping("/{customerId}")
    public ResponseEntity<?> approveKycRequest(@PathVariable("customerId") String customerId){
        return kycDetailsService.approveKycRequest(customerId);
    }
}
