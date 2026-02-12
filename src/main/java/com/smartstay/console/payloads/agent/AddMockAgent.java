package com.smartstay.console.payloads.agent;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record AddMockAgent(@NotNull(message = "Email cannot be null")
                           @NotEmpty(message = "Email cannot be empty")
                           String email,
                           @NotNull(message = "Role Id cannot be empty")
                           @Min(value = 1, message = "Role Id cannot be 0 or less")
                           Long roleId) {
}
