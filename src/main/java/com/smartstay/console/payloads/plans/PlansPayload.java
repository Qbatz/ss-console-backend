package com.smartstay.console.payloads.plans;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public record PlansPayload(@NotBlank(message = "Plan name is required")
                           String planName,
                           String planCode,
                           @NotBlank(message = "Plan type is required")
                           String planType,
                           @NotNull(message = "Duration is required")
                           @Positive(message = "Duration should be higher than 0")
                           Long duration,
                           @NotNull(message = "Price is required")
                           @Positive(message = "Price needs to be more than 0")
                           Double price,
                           @NotNull(message = "Discount is required")
                           @PositiveOrZero(message = "Discount should be 0 or higher")
                           @Max(value = 100, message = "Discount cannot be more than 100")
                           Double discountPercentage,
                           @NotNull(message = "Should show is required")
                           Boolean shouldShow,
                           @NotNull(message = "Can customize is required")
                           Boolean canCustomize,
                           @Valid
                           List<PlanFeaturesPayload> planFeatures) {
}
