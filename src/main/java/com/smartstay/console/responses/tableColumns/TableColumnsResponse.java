package com.smartstay.console.responses.tableColumns;

import com.smartstay.console.dao.ColumnFilters;

import java.util.List;

public record TableColumnsResponse(Long tableColumnId,
                                   String hostelId,
                                   String hostelName,
                                   String userId,
                                   String userName,
                                   String moduleName,
                                   List<ColumnFilters> columns,
                                   String createdAtDate,
                                   String createdAtTime,
                                   String updatedAtDate,
                                   String updatedAtTime) {
}
