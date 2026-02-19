package com.smartstay.console.payloads.owners;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPassword(@NotBlank(message = "User id is required and cannot be blank")
                            @NotEmpty(message = "User id is required and cannot be blank")
                            String userId,
                            @NotBlank(message = "Password is required and cannot be blank")
                            @Size(min = 8, max = 100, message = "Password must be at least 8 characters and less than 100")
                            @Pattern(
                                    regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$",
                                    message = "Password must contain at least 1 uppercase, 1 lowercase, 1 number, and 1 special character"
                            )
                            String password,
                            @NotBlank(message = "Confirm password is required and cannot be blank")
                            @Size(min = 8, max = 100, message = "Confirm password must be at least 8 characters and less than 100")
                            @Pattern(
                                    regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$",
                                    message = "Password must contain at least 1 uppercase, 1 lowercase, 1 number, and 1 special character"
                            )
                            String confirmPassword
                            ) {
}
