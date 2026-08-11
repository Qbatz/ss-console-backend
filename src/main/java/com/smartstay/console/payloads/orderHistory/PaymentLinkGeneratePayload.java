package com.smartstay.console.payloads.orderHistory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record PaymentLinkGeneratePayload(@NotBlank(message = "Plan code is required")
                                         String planCode,
                                         @NotNull(message = "Discount amount is required")
                                         @PositiveOrZero(message = "Discount amount must not be less than 0")
                                         Double discountAmount,
                                         @NotBlank(message = "Paid by is required")
                                         String paidBy) {
}
