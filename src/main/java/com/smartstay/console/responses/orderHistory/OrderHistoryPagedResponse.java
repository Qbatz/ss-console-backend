package com.smartstay.console.responses.orderHistory;

public record OrderHistoryPagedResponse(double totalRevenue,
                                        StatusOrderHistoryPagedResponse paidHistories,
                                        StatusOrderHistoryPagedResponse createdHistories) {
}
