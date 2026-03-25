package com.smartstay.console.responses.agents;

import java.util.List;

public record AgentListRes(List<AgentResponse> activeAgents,
                           List<AgentResponse> inActiveAgents) {
}
