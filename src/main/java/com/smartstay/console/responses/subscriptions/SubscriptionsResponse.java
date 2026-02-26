package com.smartstay.console.responses.subscriptions;

public record SubscriptionsResponse(Long subscriptionId,
                                    String subscriptionNumber,
                                    String hostelId,
                                    String hostelName,
                                    String hostelInitials,
                                    String planCode,
                                    String planName,
                                    String planStartsAt,
                                    String planEndsAt,
                                    Double planAmount,
                                    Double paidAmount) {
}
