package com.smartstay.console.responses.orderHistory;

import java.util.List;

public record OrderHistoryGraphResponse(OrderHistoryGraphCardDataRes cardData,
                                        OrderHistoryGraphDataRes current,
                                        OrderHistoryGraphDataRes comparison,
                                        List<OrderHistoryGraphFilterRes> comparisonFilters) {
}
