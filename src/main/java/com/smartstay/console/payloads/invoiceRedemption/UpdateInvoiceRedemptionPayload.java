package com.smartstay.console.payloads.invoiceRedemption;

import jakarta.validation.constraints.Min;

public record UpdateInvoiceRedemptionPayload(@Min(value = 0, message = "Amount can not be lower than 0")
                                             double amount) {
}
