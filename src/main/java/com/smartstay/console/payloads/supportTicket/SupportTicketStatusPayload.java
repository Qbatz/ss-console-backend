package com.smartstay.console.payloads.supportTicket;

import jakarta.validation.constraints.NotBlank;

public record SupportTicketStatusPayload(@NotBlank(message = "Ticket status is required")
                                         String ticketStatus,
                                         String comments,
                                         String agentId,
                                         String priority) {
}
