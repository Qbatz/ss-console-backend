package com.smartstay.console.dto.plans;

import java.util.Date;
import java.util.List;

public record PlanSnapshot(Long planId,
                           String planName,
                           Double price,
                           Long duration,
                           Double discounts,
                           String planType,
                           String planCode,
                           boolean shouldShow,
                           boolean canCustomize,
                           boolean isActive,
                           Double gst,
                           Double gstAmount,
                           Double cgst,
                           Double sgst,
                           Double cgstAmount,
                           Double sgstAmount,
                           Double finalPrice,
                           Date createdAt,
                           Date updatedAt,
                           List<PlanFeatureSnapshot> featuresList) {
}
