package com.smartstay.console.payloads.orderHistory;

import jakarta.validation.constraints.NotBlank;

public record PaymentLinkSharePayload(@NotBlank(message = "PaymentLink is required")
                                      String paymentLink) {
}
