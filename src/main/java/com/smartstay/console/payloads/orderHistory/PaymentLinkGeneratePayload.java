package com.smartstay.console.payloads.orderHistory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PaymentLinkGeneratePayload(@NotBlank(message = "Plan code is required")
                                         String planCode,
                                         @NotNull(message = "Discount amount is required")
                                         @Positive(message = "Discount amount must be greater than 0")
                                         Double discountAmount) {
}
