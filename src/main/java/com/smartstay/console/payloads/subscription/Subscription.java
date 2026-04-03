package com.smartstay.console.payloads.subscription;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record Subscription(@NotNull(message = "IsTrial is required")
                           Boolean isTrial,
                           @PositiveOrZero(message = "Trial days must not be less than 0")
                           int trialDays,
                           String planCode,
                           @PositiveOrZero(message = "Paid Amount must not be less than 0")
                           Double paidAmount,
                           @PositiveOrZero(message = "Discount Amount must not be less than 0")
                           Double discountAmount) {
}
