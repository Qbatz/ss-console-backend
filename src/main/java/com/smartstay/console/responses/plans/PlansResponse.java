package com.smartstay.console.responses.plans;

import java.util.List;

public record PlansResponse(Long planId,
                            String planName,
                            String planCode,
                            String planType,
                            Long duration,
                            Double price,
                            Double discountPercentage,
                            boolean shouldShow,
                            boolean canCustomize,
                            String createdAtDate,
                            String createdAtTime,
                            String updatedAtDate,
                            String updatedAtTime,
                            List<PlanFeaturesResponse> planFeatures) {
}
