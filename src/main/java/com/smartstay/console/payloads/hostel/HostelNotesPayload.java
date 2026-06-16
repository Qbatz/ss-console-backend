package com.smartstay.console.payloads.hostel;

import jakarta.validation.constraints.NotBlank;

public record HostelNotesPayload(@NotBlank(message = "Notes is required")
                                 String notes) {
}
