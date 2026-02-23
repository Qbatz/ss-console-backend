package com.smartstay.console.responses.agents;

public record AgentResponse(String agentId,
                            String firstName,
                            String lastName,
                            String fullName,
                            String initials,
                            String email,
                            String mobile,
                            Long roleId,
                            String roleName,
                            String lastActiveDate,
                            String lastActiveTime) {
}
