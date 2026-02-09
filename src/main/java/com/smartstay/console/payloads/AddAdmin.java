package com.smartstay.console.payloads;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record AddAdmin(
        @NotNull(message = "Email Id cannot be empty")
        @NotEmpty(message = "Email Id cannot be empty")
        String emailId,
        @NotNull(message = "Role Id cannot be empty")
        Long roleId,
        @NotNull(message = "Ticket is required to add admin")
                @NotEmpty(message = "Ticket is required to add admin")
        String ticketLink){
}
