package com.smartstay.console.responses.hostels;

public record SubscriptionResponse(Long subscriptionId,
                                   String subscriptionNumber,
                                   String planCode,
                                   String planName,
                                   String planStartsAt,
                                   String planEndsAt,
                                   Double planAmount,
                                   Double paidAmount) {
}
