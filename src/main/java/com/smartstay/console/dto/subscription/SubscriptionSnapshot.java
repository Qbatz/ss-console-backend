package com.smartstay.console.dto.subscription;

import java.util.Date;

public record SubscriptionSnapshot(Long subscriptionId,
                                   String subscriptionNumber,
                                   Long orderId,
                                   String hostelId,
                                   String planCode,
                                   String planName,
                                   Date planStartsAt,
                                   Date planEndsAt,
                                   Date activatedAt,
                                   Double paidAmount,
                                   Double planAmount,
                                   Double discount,
                                   Double discountAmount,
                                   Date nextBillingAt,
                                   String createdBy,
                                   String createdByUserType,
                                   Date createdAt,
                                   Boolean isActive,
                                   String paymentProof,
                                   String invoiceUrl,
                                   String generationType) {
}
