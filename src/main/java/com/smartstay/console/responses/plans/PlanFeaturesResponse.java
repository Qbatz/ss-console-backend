package com.smartstay.console.responses.plans;

public record PlanFeaturesResponse(Long planFeatureId,
                                   Long smartstayFeatureId,
                                   String featureName,
                                   Double price,
                                   boolean isFeatureActive,
                                   String labelText,
                                   String labelDescription,
                                   String startsFrom,
                                   String endsAt) {
}
