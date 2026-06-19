package com.smartstay.console.controller;

import com.smartstay.console.payloads.invoice.AdvanceBalanceAmountPayload;
import com.smartstay.console.payloads.invoice.InvoiceIdMobilePayload;
import com.smartstay.console.services.InvoiceV1Service;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v2/invoice")
@CrossOrigin(origins = "*")
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "Authorization")
public class InvoiceController {

    @Autowired
    private InvoiceV1Service invoiceService;

    @GetMapping("/{hostelId}")
    public ResponseEntity<?> getInvoicesByHostelId(@PathVariable("hostelId") String hostelId,
                                                   @RequestParam(value = "page", defaultValue = "0") int page,
                                                   @RequestParam(value = "size", defaultValue = "10") int size) {
        return invoiceService.getInvoicesByHostelId(hostelId, page, size);
    }

    @DeleteMapping
    public ResponseEntity<?> deleteInvoicesByIds(@RequestBody List<InvoiceIdMobilePayload> payloads) {
        return invoiceService.deleteInvoicesByIds(payloads);
    }

    @GetMapping("/receipt/{hostelId}/{invoiceId}")
    public ResponseEntity<?> getReceiptsById(@PathVariable("hostelId") String hostelId,
                                             @PathVariable("invoiceId") String invoiceId) {
        return invoiceService.getReceiptsByInvoiceId(hostelId, invoiceId);
    }

    @PutMapping("/balance/{hostelId}/{invoiceId}")
    public ResponseEntity<?> updateAdvanceInvoiceBalance(@PathVariable("hostelId") String hostelId,
                                                         @PathVariable("invoiceId") String invoiceId,
                                                         @Valid @RequestBody AdvanceBalanceAmountPayload payload) {
        return invoiceService.updateAdvanceInvoiceBalance(hostelId, invoiceId, payload);
    }
}
