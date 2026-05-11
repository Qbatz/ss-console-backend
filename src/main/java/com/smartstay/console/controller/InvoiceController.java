package com.smartstay.console.controller;

import com.smartstay.console.payloads.invoice.InvoiceIdAmountPayload;
import com.smartstay.console.services.InvoiceV1Service;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
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
    public ResponseEntity<?> deleteInvoicesByIds(@RequestBody List<InvoiceIdAmountPayload> payloads) {
        return invoiceService.deleteInvoicesByIds(payloads);
    }
}
