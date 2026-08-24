package com.smartstay.console.payloads.productUpdate;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record ProductUpdateEditPayload(String title,
                                       String description,
                                       String version,
                                       @JsonFormat(pattern = "dd-MM-yyyy")
                                       LocalDate releaseDate,
                                       String updateType,
                                       String platform,
                                       String audience,
                                       List<String> audienceIds,
                                       String publishStatus,
                                       @JsonFormat(pattern = "dd-MM-yyyy")
                                       LocalDate publishDate,
                                       @JsonFormat(pattern = "HH:mm")
                                       LocalTime publishTime,
                                       @JsonFormat(pattern = "dd-MM-yyyy")
                                       LocalDate expiryDate) {
}
