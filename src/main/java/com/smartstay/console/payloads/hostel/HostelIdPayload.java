package com.smartstay.console.payloads.hostel;

import jakarta.validation.constraints.NotBlank;

public record HostelIdPayload(@NotBlank(message = "HostelId is required")
                              String hostelId) {
}
