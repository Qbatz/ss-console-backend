package com.smartstay.console.payloads.customers;

import jakarta.validation.constraints.NotBlank;

public record CustomerMobilePayload(@NotBlank(message = "Tenant mobile number is required")
                                    String tenantMobile) {
}
