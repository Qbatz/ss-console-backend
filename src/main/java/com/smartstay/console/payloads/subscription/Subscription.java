package com.smartstay.console.payloads.subscription;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record Subscription(Integer trialDays,
                           @NotNull(message = "Plan code is required")
                           @NotEmpty(message = "Plan code is required")
                           String planCode,
                           Double paidAmount,
                           Double discountAmount,
                           String paidBy) {
}
