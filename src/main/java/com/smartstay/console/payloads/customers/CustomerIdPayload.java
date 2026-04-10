package com.smartstay.console.payloads.customers;

import jakarta.validation.constraints.NotBlank;

public record CustomerIdPayload(@NotBlank(message = "customerId is required")
                                String customerId) {
}
