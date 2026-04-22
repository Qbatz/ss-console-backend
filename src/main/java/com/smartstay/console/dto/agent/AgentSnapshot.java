package com.smartstay.console.dto.agent;

import java.util.Date;

public record AgentSnapshot(String agentId,
                            String firstName,
                            String lastName,
                            String mobile,
                            String agentEmailId,
                            Long roleId,
                            String agentZohoUserId,
                            String ticketLink,
                            Boolean isActive,
                            Boolean isProfileCompleted,
                            boolean isMockAgent,
                            Date createdAt,
                            String createdBy,
                            Date updatedAt,
                            String updatedBy) {
}
