package com.smartstay.console.payloads.plans;

import jakarta.validation.constraints.NotBlank;

public record PlanFeaturesUpdatePayload(@NotBlank(message = "PlanFeature Id is required")
                                        Long planFeatureId,
                                        String featureName,
                                        Double price) {
}
