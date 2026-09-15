package com.smartstay.console.payloads.serviceToken;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.time.LocalTime;

public record GenerateServiceTokenPayload(@NotBlank(message = "Service is required")
                                          String service,
                                          String secret,
                                          @JsonFormat(pattern = "dd-MM-yyyy")
                                          LocalDate expiryDate,
                                          @JsonFormat(pattern = "HH:mm")
                                          LocalTime expiryTime) {
}
