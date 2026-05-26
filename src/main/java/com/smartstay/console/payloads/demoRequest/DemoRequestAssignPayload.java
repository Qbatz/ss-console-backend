package com.smartstay.console.payloads.demoRequest;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record DemoRequestAssignPayload (@NotNull(message = "AgentId cannot be null")
                                        @NotEmpty(message = "AgentId cannot be empty")
                                        String agentId,
                                        String comments){
}
