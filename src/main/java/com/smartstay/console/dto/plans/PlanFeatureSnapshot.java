package com.smartstay.console.dto.plans;

import java.util.Date;

public record PlanFeatureSnapshot(Long planFeatureId,
                                  Long smartstayFeatureId,
                                  String featureName,
                                  Double price,
                                  boolean isFeatureActive,
                                  String labelText,
                                  String labelDescription,
                                  Date startsFrom,
                                  Date endsAt,
                                  boolean isActive,
                                  Long planId) {
}
