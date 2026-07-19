package com.smartstay.console.responses.plans;

public record SmartstayFeaturesResponse(Long smartstayFeatureId,
                                        String featureName,
                                        boolean isCommon,
                                        String createdAtDate,
                                        String createdAtTime,
                                        String updatedAtDate,
                                        String updatedAtTime) {
}
