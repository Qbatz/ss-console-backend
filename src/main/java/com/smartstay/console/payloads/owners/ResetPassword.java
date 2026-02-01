package com.smartstay.console.payloads.owners;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record ResetPassword(
        @NotBlank(message = "Password is required and cannot be blank")
                            @Size(min = 8, max = 100, message = "Password must be at least 8 characters and less than 100")
                            String password,
        @NotBlank(message = "Email id is required and cannot be blank")
        @NotEmpty(message = "Email id is required and cannot be blank")
        String emailId) {
}
