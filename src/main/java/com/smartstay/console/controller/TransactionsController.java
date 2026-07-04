package com.smartstay.console.controller;

import com.smartstay.console.payloads.customers.CustomerMobilePayload;
import com.smartstay.console.services.TransactionV1Service;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v2/receipt")
@CrossOrigin(origins = "*")
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "Authorization")
public class TransactionsController {

    @Autowired
    private TransactionV1Service transactionService;

    @DeleteMapping("/{transactionId}")
    public ResponseEntity<?> deleteTransaction(@PathVariable("transactionId") String transactionId,
                                               @Valid @RequestBody CustomerMobilePayload payload) {
        return transactionService.deleteTransactionById(transactionId, payload);
    }
}
