package com.smartstay.console.payloads.hostel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RecurringConfigurationPayload(@NotBlank(message = "HostelId is required")
                                            String hostelId,
                                            @NotNull(message = "Should verify is required")
                                            boolean shouldVerify) {
}
