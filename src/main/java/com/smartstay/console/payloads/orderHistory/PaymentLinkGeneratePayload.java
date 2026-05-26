package com.smartstay.console.payloads.orderHistory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record PaymentLinkGeneratePayload(@NotBlank(message = "Plan code is required")
                                         String planCode,
                                         @NotNull(message = "Paid amount is required")
                                         @PositiveOrZero(message = "Paid amount must be positive or zero")
                                         Double paidAmount,
                                         @NotNull(message = "Discount amount is required")
                                         @PositiveOrZero(message = "Discount amount must be positive or zero")
                                         Double discountAmount) {
}
