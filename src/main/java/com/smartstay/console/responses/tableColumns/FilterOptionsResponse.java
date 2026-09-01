package com.smartstay.console.responses.tableColumns;

import com.smartstay.console.dao.ColumnFilters;

import java.util.List;

public record FilterOptionsResponse(Long filterOptionId,
                                    String moduleName,
                                    List<ColumnFilters> filterOptions,
                                    String createdAtDate,
                                    String createdAtTime) {
}
