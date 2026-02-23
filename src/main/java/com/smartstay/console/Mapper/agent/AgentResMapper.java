package com.smartstay.console.Mapper.agent;

import com.smartstay.console.dao.Agent;
import com.smartstay.console.dao.AgentActivities;
import com.smartstay.console.dao.AgentRoles;
import com.smartstay.console.responses.agents.AgentResponse;
import com.smartstay.console.utils.Utils;

import java.util.function.Function;

public class AgentResMapper implements Function<Agent, AgentResponse> {

    AgentRoles role;
    AgentActivities agentActivities;

    public AgentResMapper(AgentRoles role,
                          AgentActivities agentActivities) {
        this.role = role;
        this.agentActivities = agentActivities;
    }

    @Override
    public AgentResponse apply(Agent agent) {

        StringBuilder fullName = new StringBuilder();
        StringBuilder initials = new StringBuilder();
        if (agent.getFirstName() != null) {
            fullName.append(agent.getFirstName());
            initials.append(agent.getFirstName().trim().toUpperCase().charAt(0));
        }
        if (agent.getLastName() != null && !agent.getLastName().trim().equalsIgnoreCase("")) {
            fullName.append(" ");
            fullName.append(agent.getLastName());
            initials.append(agent.getLastName().trim().toUpperCase().charAt(0));
        }
        else {
            if (agent.getFirstName() != null) {
                String[] nameArr = agent.getFirstName().split(" ");
                if (nameArr.length > 1) {
                    initials.append(nameArr[nameArr.length - 1].trim().toUpperCase().charAt(0));
                }
                else {
                    if (!nameArr[0].isEmpty()) {
                        initials.append(nameArr[0].toUpperCase().charAt(1));
                    }
                }
            }
        }

        String roleName = null;

        if (role != null){
            roleName = role.getRoleName();
        }

        String lastActiveDate = null;
        String lastActiveTime = null;

        if (agentActivities != null){
            lastActiveDate = Utils.dateToString(agentActivities.getCreatedAt());
            lastActiveTime = Utils.dateToTime(agentActivities.getCreatedAt());
        }

        return new AgentResponse(agent.getAgentId(), agent.getFirstName(), agent.getLastName(),
                fullName.toString(), initials.toString(), agent.getAgentEmailId(), agent.getMobile(),
                agent.getRoleId(), roleName, lastActiveDate, lastActiveTime);
    }
}
