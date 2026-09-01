package com.smartstay.console.payloads.tableColumns;

import com.smartstay.console.dao.ColumnFilters;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record EditFilterOptionsPayload(@NotBlank(message = "Module name is required")
                                       String moduleName,
                                       List<ColumnFilters> columns) {
}
