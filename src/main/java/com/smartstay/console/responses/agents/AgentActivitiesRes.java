package com.smartstay.console.responses.agents;

public record AgentActivitiesRes(Long activityId,
                                 String agentId,
                                 String agentName,
                                 String activityType,
                                 String description,
                                 String source,
                                 String sourceId,
                                 String createdAtDate,
                                 String createdAtTime) {
}
