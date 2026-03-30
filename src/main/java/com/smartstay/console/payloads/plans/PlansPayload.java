package com.smartstay.console.payloads.plans;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PlansPayload(@NotBlank(message = "Plan name is required")
                           String planName,
                           String planCode,
                           @NotBlank(message = "Plan type is required")
                           String planType,
                           @NotNull(message = "Duration is required")
                           Long duration,
                           @NotNull(message = "Price is required")
                           Double price,
                           @NotNull(message = "Discount is required")
                           Double discountPercentage,
                           @NotNull(message = "Should show is required")
                           Boolean shouldShow,
                           @NotNull(message = "Can customize is required")
                           Boolean canCustomize,
                           List<PlanFeaturesPayload> planFeatures) {
}
