package com.smartstay.console.payloads.tableColumns;

import jakarta.validation.constraints.NotBlank;

public record ResetTableColumnsPayload(@NotBlank(message = "HostelId is required")
                                       String hostelId,
                                       @NotBlank(message = "UserId is required")
                                       String userId,
                                       @NotBlank(message = "Module name is required")
                                       String moduleName) {
}
