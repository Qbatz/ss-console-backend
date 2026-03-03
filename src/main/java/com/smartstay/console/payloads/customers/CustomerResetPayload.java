package com.smartstay.console.payloads.customers;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CustomerResetPayload(@NotNull(message = "Tenant mobile number can't be null")
                                   @NotEmpty(message = "Tenant mobile number is required")
                                   String tenantMobile) {
}
