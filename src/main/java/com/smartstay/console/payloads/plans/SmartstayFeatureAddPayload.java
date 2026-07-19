package com.smartstay.console.payloads.plans;

import jakarta.validation.constraints.NotBlank;

public record SmartstayFeatureAddPayload(@NotBlank(message = "Feature name is required")
                                         String featureName,
                                         Boolean isCommon) {
}
