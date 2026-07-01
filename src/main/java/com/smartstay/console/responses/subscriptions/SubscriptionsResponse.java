package com.smartstay.console.responses.subscriptions;

public record SubscriptionsResponse(Long subscriptionId,
                                    String subscriptionNumber,
                                    Long orderHistoryId,
                                    String hostelId,
                                    String hostelName,
                                    String hostelInitials,
                                    String planCode,
                                    String planName,
                                    String planStartsAt,
                                    String planEndsAt,
                                    boolean isExpired,
                                    Double planAmount,
                                    Double paidAmount,
                                    Double discount,
                                    Double discountAmount,
                                    String paymentProof,
                                    String createdBy,
                                    String createdAtDate,
                                    String createdAtTime) {
}
