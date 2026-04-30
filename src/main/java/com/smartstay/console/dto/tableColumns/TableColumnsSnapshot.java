package com.smartstay.console.dto.tableColumns;

import java.util.Date;
import java.util.List;

public record TableColumnsSnapshot(Long columnId,
                                   String hostelId,
                                   String userId,
                                   String moduleName,
                                   List<ColumnFiltersSnapshot> columns,
                                   boolean isActive,
                                   Date createdAt,
                                   Date updatedAt) {
}
