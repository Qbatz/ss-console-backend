package com.smartstay.console.payloads.demoRequest;

import jakarta.validation.constraints.NotBlank;

public record DemoRequestCommentPayload(@NotBlank(message = "Comment is required")
                                        String comment) {
}
