package com.smartstay.console.payloads.hostel;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record HostelIdPayload(@NotNull(message = "HostelId can't be null")
                              @NotEmpty(message = "HostelId is required")
                              String hostelId) {
}
