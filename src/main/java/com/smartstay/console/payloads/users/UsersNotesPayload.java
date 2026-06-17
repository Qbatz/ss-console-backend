package com.smartstay.console.payloads.users;

import jakarta.validation.constraints.NotBlank;

public record UsersNotesPayload(@NotBlank(message = "Notes is required")
                                String notes) {
}
