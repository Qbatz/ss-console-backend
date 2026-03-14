package com.smartstay.console.payloads.agent;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record AgentIdPayload(@NotNull(message = "AgentId cannot be null")
                             @NotEmpty(message = "AgentId cannot be empty")
                             String agentId) {
}