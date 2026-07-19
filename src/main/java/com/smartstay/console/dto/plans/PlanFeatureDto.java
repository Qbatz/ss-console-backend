package com.smartstay.console.dto.plans;

public record PlanFeatureDto(Long planFeatureId,
                             Long smartStayFeatureId,
                             String featureName,
                             boolean isCommon,
                             Double price,
                             boolean isFeatureActive,
                             String labelText,
                             String labelDescription,
                             String startsFrom,
                             String endsAt) {
}
