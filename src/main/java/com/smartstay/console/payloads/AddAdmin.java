package com.smartstay.console.payloads;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record AddAdmin(
        @NotNull(message = "Email Id cannot be empty")
        @NotEmpty(message = "Email Id cannot be empty")
        String emailId) {
}
