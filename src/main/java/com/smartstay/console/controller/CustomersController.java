package com.smartstay.console.controller;

import com.smartstay.console.payloads.customers.CustomerResetPayload;
import com.smartstay.console.services.CustomersService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
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

    @GetMapping("/{customerId}")
    public ResponseEntity<?> getTenantDetails(@PathVariable("customerId") String customerId) {
        return customersService.getTenantDetails(customerId);
    }

    @GetMapping("/tenant-summary")
    public ResponseEntity<?> getTenantsWithPaymentSummary(@RequestParam(value = "page", defaultValue = "0") int page,
                                                          @RequestParam(value = "size", defaultValue = "10") int size,
                                                          @RequestParam(value = "tenantName", required = false) String tenantName){
        return customersService.getTenantsWithPaymentSummary(page, size, tenantName);
    }

    @DeleteMapping("/{hostelId}/{customerId}")
    public ResponseEntity<?> deleteTenant(@PathVariable("hostelId") String hostelId,
                                          @PathVariable("customerId") String customerId,
                                          @Valid @RequestBody CustomerResetPayload customerResetPayload) {
        return customersService.deleteTenant(hostelId, customerId, customerResetPayload);
    }

    @GetMapping("/deductions/{hostelId}/{customerId}")
    public ResponseEntity<?> getCustomerDeductions(@PathVariable("hostelId") String hostelId,
                                                   @PathVariable("customerId") String customerId){
        return customersService.getCustomerDeductions(hostelId, customerId);
    }

    @PutMapping("/deductions/{hostelId}/{customerId}/{invoiceId}")
    public ResponseEntity<?> updateDeductions(@PathVariable("hostelId") String hostelId,
                                              @PathVariable("customerId") String customerId,
                                              @PathVariable("invoiceId") String invoiceId){
        return customersService.updateDeductions(hostelId, customerId, invoiceId);
    }

    @GetMapping("/deductions")
    public ResponseEntity<?> getCustomersWithPendingAdvanceDeductions(){
        return customersService.getCustomersWithPendingAdvanceDeductions();
    }
}
