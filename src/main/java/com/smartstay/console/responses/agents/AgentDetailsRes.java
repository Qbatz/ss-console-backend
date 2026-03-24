package com.smartstay.console.responses.agents;

import java.util.List;

public record AgentDetailsRes(String agentId,
                              String firstName,
                              String lastName,
                              String fullName,
                              String initials,
                              String email,
                              String mobile,
                              Long roleId,
                              String roleName,
                              String agentZohoUserId,
                              String ticketLink,
                              Boolean isProfileCompleted,
                              String createdAtDate,
                              String createdAtTime,
                              String createdBy,
                              String updatedAtDate,
                              String updatedAtTime,
                              String updatedBy,
                              List<AgentActivitiesRes> agentActivities) {
}
