package com.smartstay.console.payloads.tableColumns;

import com.smartstay.console.dao.ColumnFilters;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record EditTableColumnsPayload(@NotBlank(message = "HostelId is required")
                                      String hostelId,
                                      @NotBlank(message = "UserId is required")
                                      String userId,
                                      @NotBlank(message = "Module name is required")
                                      String moduleName,
                                      List<ColumnFilters> columns) {
}
