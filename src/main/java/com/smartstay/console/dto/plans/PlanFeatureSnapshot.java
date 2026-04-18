package com.smartstay.console.dto.plans;

public record PlanFeatureSnapshot(Long planFeatureId,
                                  String featureName,
                                  Double price,
                                  boolean isActive,
                                  Long planId) {
}
