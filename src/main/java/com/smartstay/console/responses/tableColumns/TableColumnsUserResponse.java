package com.smartstay.console.responses.tableColumns;

import java.util.List;

public record TableColumnsUserResponse(String userId,
                                       String userName,
                                       List<TableColumnsResponse> tableColumns) {
}
