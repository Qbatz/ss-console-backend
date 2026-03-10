package com.smartstay.console.payloads.owners;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record UserEmailPayload(@NotNull(message = "Email can't be null")
                               @NotEmpty(message = "Email can't be empty")
                               String newEmail) {
}
