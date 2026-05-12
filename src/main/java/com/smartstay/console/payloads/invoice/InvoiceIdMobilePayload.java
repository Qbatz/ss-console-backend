package com.smartstay.console.payloads.invoice;

import jakarta.validation.constraints.NotBlank;

public record InvoiceIdMobilePayload(@NotBlank(message = "InvoiceId is required")
                                     String invoiceId,
                                     @NotBlank(message = "Tenant mobile number is required")
                                     String tenantMobile) {
}
