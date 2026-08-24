package com.smartstay.console.payloads.productUpdate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductUpdateItemAddPayload(@NotNull
                                          Long productUpdateId,
                                          @NotBlank(message = "Title is required")
                                          String title,
                                          @NotBlank(message = "Description is required")
                                          String description,
                                          @NotBlank(message = "Type is required")
                                          String updateType,
                                          @NotBlank(message = "Module is required")
                                          String module,
                                          @NotBlank(message = "CTA is required")
                                          String cta,
                                          String ctaLink,
                                          @NotBlank(message = "ClientId is required")
                                          String clientId) {
}
