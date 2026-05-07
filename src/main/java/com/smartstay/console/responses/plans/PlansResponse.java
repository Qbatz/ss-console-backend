package com.smartstay.console.responses.plans;

import java.util.List;

public record PlansResponse(Long planId,
                            String planName,
                            String planCode,
                            String planType,
                            Long duration,
                            Double price,
                            Double discountPercentage,
                            Double gst,
                            Double cgst,
                            Double sgst,
                            Double gstAmount,
                            Double cgstAmount,
                            Double sgstAmount,
                            Double finalPrice,
                            boolean shouldShow,
                            boolean canCustomize,
                            String createdAtDate,
                            String createdAtTime,
                            String updatedAtDate,
                            String updatedAtTime,
                            List<PlanFeaturesResponse> planFeatures) {
}
