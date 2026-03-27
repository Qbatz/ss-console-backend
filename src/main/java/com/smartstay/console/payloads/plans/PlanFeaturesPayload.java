package com.smartstay.console.payloads.plans;

import jakarta.validation.constraints.NotBlank;

public record PlanFeaturesPayload(@NotBlank(message = "Feature name is required")
                                  String featureName,
                                  Double price) {
}
