package com.smartstay.console.dto.tableColumns;

import java.util.Date;
import java.util.List;

public record FilterOptionsSnapshot(Long filterOptionId,
                                    String moduleName,
                                    List<ColumnFiltersSnapshot> filterOptions,
                                    Boolean isActive,
                                    Date createdAt) {
}
