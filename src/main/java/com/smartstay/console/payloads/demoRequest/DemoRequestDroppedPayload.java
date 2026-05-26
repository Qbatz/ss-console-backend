package com.smartstay.console.payloads.demoRequest;

import jakarta.validation.constraints.NotBlank;

public record DemoRequestDroppedPayload(String comments,
                                        @NotBlank(message = "Drop reason is required")
                                        String dropReason) {
}
