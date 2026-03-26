package com.smartstay.console.payloads.plans;

public record PlanFeaturesUpdatePayload(Long planFeatureId,
                                        String featureName,
                                        Double price) {
}
