package com.smartstay.console.dto.orderHistory;

public record PaymentLinkGenerateDto(double amount,
                                     String currency,
                                     String description,
                                     String planCode,
                                     double discountAmount,
                                     double planPrice,
                                     String createdBy) {
}
