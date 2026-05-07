package com.smartstay.console.payloads.invoiceRedemption;

import jakarta.validation.constraints.Min;

public record UpdateInvoiceRedemptionPayload(@Min(value = 1, message = "Amount can not be 0 or lower")
                                             double amount) {
}
