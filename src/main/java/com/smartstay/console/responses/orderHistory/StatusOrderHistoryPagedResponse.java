package com.smartstay.console.responses.orderHistory;

import java.util.List;

public record StatusOrderHistoryPagedResponse(int currentPage,
                                              int pageSize,
                                              long totalItems,
                                              int totalPages,
                                              List<OrderHistoryResponse> orderHistories) {
}
