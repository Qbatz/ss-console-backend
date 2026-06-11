package com.smartstay.console.payloads.owners;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserEmailPayload(@NotBlank(message = "Email is required")
                               @Email(message = "Invalid email format")
                               String newEmail) {
}
