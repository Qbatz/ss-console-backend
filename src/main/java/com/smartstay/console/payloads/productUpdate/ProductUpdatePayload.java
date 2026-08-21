package com.smartstay.console.payloads.productUpdate;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record ProductUpdatePayload(@NotBlank(message = "Title is required")
                                   String title,
                                   @NotBlank(message = "Description is required")
                                   String description,
                                   String version,
                                   @JsonFormat(pattern = "dd-MM-yyyy")
                                   LocalDate releaseDate,
                                   @NotBlank(message = "Type is required")
                                   String updateType,
                                   @NotBlank(message = "Platform is required")
                                   String platform,
                                   @NotBlank(message = "Audience is required")
                                   String audience,
                                   List<String> audienceIds,
                                   @NotBlank(message = "Publish status is required")
                                   String publishStatus,
                                   @JsonFormat(pattern = "dd-MM-yyyy")
                                   LocalDate publishDate,
                                   @JsonFormat(pattern = "HH:mm")
                                   LocalTime publishTime,
                                   @JsonFormat(pattern = "dd-MM-yyyy")
                                   LocalDate expiryDate,
                                   @NotNull(message = "Product update items is required")
                                   @NotEmpty(message = "Product update items can not be empty")
                                   @Valid
                                   List<ProductUpdateItemPayload> productUpdateItems) {
}
