package com.smartstay.console.payloads.customers;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CustomerJoiningDatePayload(@NotNull(message = "New joining date is required")
                                         @JsonFormat(pattern = "dd-MM-yyyy")
                                         LocalDate newJoiningDate,
                                         @NotBlank(message = "Tenant mobile number is required")
                                         String tenantMobile,
                                         @NotBlank(message = "CustomerId is required")
                                         String customerId) {
}
