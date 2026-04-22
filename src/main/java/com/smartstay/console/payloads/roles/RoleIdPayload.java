package com.smartstay.console.payloads.roles;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RoleIdPayload(@NotNull(message = "Role Id cannot be empty")
                            @Min(value = 1, message = "Role Id cannot be 0 or less")
                            Long roleId) {
}
