package com.smartstay.console.payloads.serviceToken;

import jakarta.validation.constraints.NotBlank;

public record GenerateServiceTokenPayload(@NotBlank(message = "Service is required")
                                          String service) {
}
