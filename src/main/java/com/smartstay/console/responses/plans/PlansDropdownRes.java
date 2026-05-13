package com.smartstay.console.responses.plans;

public record PlansDropdownRes(Long planId,
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
                               Double yearlyPrice) {
}
