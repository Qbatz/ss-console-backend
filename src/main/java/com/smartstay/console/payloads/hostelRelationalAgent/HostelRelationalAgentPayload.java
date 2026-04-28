package com.smartstay.console.payloads.hostelRelationalAgent;

import jakarta.validation.constraints.NotBlank;

public record HostelRelationalAgentPayload(@NotBlank(message = "AgentId is required")
                                           String agentId,
                                           @NotBlank(message = "Reason is required")
                                           String reason,
                                           String comments) {
}
