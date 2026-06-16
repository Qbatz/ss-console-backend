package com.smartstay.console.payloads.supportTicket;

import jakarta.validation.constraints.NotBlank;

public record SupportTicketAssignPayload(@NotBlank(message = "AgentId is required")
                                         String agentId,
                                         String comments,
                                         @NotBlank(message = "Priority is required")
                                         String priority) {
}
