package com.smartstay.console.controller;

import com.smartstay.console.services.CustomersService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v2/tenants")
@CrossOrigin(origins = "*")
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "Authorization")
public class CustomersController {

    @Autowired
    CustomersService customersService;

    @GetMapping("/tenant-summary")
    public ResponseEntity<?> getTenantsWithPaymentSummary(@RequestParam(value = "page", defaultValue = "0") int page,
                                                          @RequestParam(value = "size", defaultValue = "10") int size,
                                                          @RequestParam(value = "tenantName", required = false) String tenantName){
        return customersService.getTenantsWithPaymentSummary(page, size, tenantName);
    }
}
