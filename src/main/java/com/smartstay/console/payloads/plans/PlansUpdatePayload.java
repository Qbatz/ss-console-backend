package com.smartstay.console.payloads.plans;

import java.util.List;

public record PlansUpdatePayload(String planName,
                                 String planCode,
                                 String planType,
                                 Long duration,
                                 Double price,
                                 Double discountPercentage,
                                 boolean shouldShow,
                                 boolean canCustomize,
                                 List<PlanFeaturesUpdatePayload> planFeatures) {
}
