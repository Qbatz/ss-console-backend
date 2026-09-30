package com.smartstay.console.responses.orderHistory;

public record OrderHistoryGraphCardDataRes(String period,
                                           Integer year,
                                           double totalRevenue,
                                           double revenuePercentageDifference,
                                           long totalSubscriptions,
                                           double subscriptionPercentageDifference) {
}
