package com.smartstay.console.responses.plans;

public record PlansDropdownRes(Long planId,
                               String planName,
                               String planCode,
                               String planType,
                               Long duration,
                               Double price,
                               Double discountPercentage) {
}
