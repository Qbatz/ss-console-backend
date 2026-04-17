package com.smartstay.console.payloads.owners;

import jakarta.validation.constraints.NotBlank;

public record UserMobilePayload(@NotBlank(message = "Mobile number is required")
                                String mobileNumber) {
}
