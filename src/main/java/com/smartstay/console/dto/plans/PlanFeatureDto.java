package com.smartstay.console.dto.plans;

public record PlanFeatureDto(Long smartStayFeatureId,
                             String featureName,
                             Double price,
                             boolean isFeatureActive) {
}
