package com.smartstay.console.controller;

import com.smartstay.console.payloads.invoiceRedemption.UpdateInvoiceRedemptionPayload;
import com.smartstay.console.services.InvoiceRedemptionService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v2/invoice-redemption")
@CrossOrigin(origins = "*")
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "Authorization")
public class InvoiceRedemptionController {

    @Autowired
    private InvoiceRedemptionService invoiceRedemptionService;

    @GetMapping
    public ResponseEntity<?> getInvoiceRedemption(@RequestParam(value = "page", defaultValue = "0") int page,
                                                  @RequestParam(value = "size", defaultValue = "10") int size,
                                                  @RequestParam(value = "name", required = false) String name) {
        return invoiceRedemptionService.getInvoiceRedemption(page, size, name);
    }

    @GetMapping("/{hostelId}")
    public ResponseEntity<?> getInvoiceRedemptionsByHostelId(@PathVariable("hostelId") String hostelId,
                                                            @RequestParam(value = "page", defaultValue = "0") int page,
                                                            @RequestParam(value = "size", defaultValue = "10") int size) {
        return invoiceRedemptionService.getInvoiceRedemptionsByHostelId(hostelId, page, size);
    }

    @PutMapping("/{invoiceRedemptionId}")
    public ResponseEntity<?> updateInvoiceRedemption(@PathVariable("invoiceRedemptionId") Long invoiceRedemptionId,
                                                     @RequestBody @Valid UpdateInvoiceRedemptionPayload payload) {
        return invoiceRedemptionService.updateInvoiceRedemption(invoiceRedemptionId, payload);
    }

    @DeleteMapping("/{invoiceRedemptionId}")
    public ResponseEntity<?> deleteInvoiceRedemption(@PathVariable("invoiceRedemptionId") Long invoiceRedemptionId) {
        return invoiceRedemptionService.deleteInvoiceRedemption(invoiceRedemptionId);
    }
}
