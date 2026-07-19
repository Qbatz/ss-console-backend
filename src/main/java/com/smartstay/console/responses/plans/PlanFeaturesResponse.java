package com.smartstay.console.responses.plans;

public record PlanFeaturesResponse(Long planFeatureId,
                                   Long smartstayFeatureId,
                                   String featureName,
                                   boolean isCommon,
                                   Double price,
                                   boolean isFeatureActive,
                                   String labelText,
                                   String labelDescription,
                                   String startsFrom,
                                   String endsAt) {
}
