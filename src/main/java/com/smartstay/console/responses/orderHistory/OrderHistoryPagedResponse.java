package com.smartstay.console.responses.orderHistory;

import java.util.List;

public record OrderHistoryPagedResponse(double totalRevenue,
                                        int currentPage,
                                        int pageSize,
                                        long totalItems,
                                        int totalPages,
                                        List<OrderHistoryResponse> orderHistories,
                                        StatusOrderHistoryPagedResponse paidHistories,
                                        StatusOrderHistoryPagedResponse createdHistories) {
}
