package com.smartstay.console.responses.orderHistory;

import java.util.List;

public record OrderHistoryGraphDataRes(String startDate,
                                       String endDate,
                                       String label,
                                       List<OrderHistoryGraphRecordRes> records) {
}
