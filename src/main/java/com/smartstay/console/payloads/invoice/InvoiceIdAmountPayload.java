package com.smartstay.console.payloads.invoice;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record InvoiceIdAmountPayload(@NotBlank(message = "InvoiceId is required")
                                     String invoiceId,
                                     @Min(value = 1, message = "Amount can not be less than 0")
                                     Double amount) {
}
