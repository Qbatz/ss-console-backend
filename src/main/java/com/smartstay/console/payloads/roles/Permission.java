package com.smartstay.console.payloads.roles;

import jakarta.validation.constraints.NotNull;

public record Permission(
        @NotNull(message = "Module ID is required")
        Integer moduleId,
        Boolean canRead,
        Boolean canWrite,
        Boolean canUpdate,
        Boolean canDelete
) {
}
