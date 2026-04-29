package com.smartstay.console.responses.tableColumns;

import java.util.List;

public record TableColumnsHostelResponse(String hostelId,
                                         String hostelName,
                                         List<TableColumnsUserResponse> usersList) {
}
