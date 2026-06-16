package com.smartstay.console.payloads.supportTicket;

import jakarta.validation.constraints.NotBlank;

public record SupportTicketNotesPayload(@NotBlank(message = "Notes is required")
                                        String notes) {
}
