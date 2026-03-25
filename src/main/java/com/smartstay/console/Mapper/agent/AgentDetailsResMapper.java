package com.smartstay.console.Mapper.agent;

import com.smartstay.console.dao.Agent;
import com.smartstay.console.dao.AgentRoles;
import com.smartstay.console.responses.agents.AgentActivitiesRes;
import com.smartstay.console.responses.agents.AgentDetailsRes;
import com.smartstay.console.utils.Utils;

import java.util.List;
import java.util.function.Function;

public class AgentDetailsResMapper implements Function<Agent, AgentDetailsRes> {

    List<AgentActivitiesRes> activities;
    AgentRoles agentRole;
    Agent createdBy;
    Agent updatedBy;

    public AgentDetailsResMapper(List<AgentActivitiesRes> activities,
                                 AgentRoles agentRole,
                                 Agent createdBy,
                                 Agent updatedBy) {
        this.activities = activities;
        this.agentRole = agentRole;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }

    @Override
    public AgentDetailsRes apply(Agent agent) {

        String roleName = null;
        if (agentRole != null){
            roleName = agentRole.getRoleName();
        }

        String createdByName = null;
        if (createdBy != null){
            createdByName = Utils.getFullName(createdBy.getFirstName(), createdBy.getLastName());
        }

        String updatedAtDate = null;
        String updatedAtTime = null;
        if (agent.getUpdatedAt() != null){
            updatedAtDate = Utils.dateToString(agent.getUpdatedAt());
            updatedAtTime = Utils.dateToTime(agent.getUpdatedAt());
        }

        String updatedByName = null;
        if (updatedBy != null){
            updatedByName = Utils.getFullName(updatedBy.getFirstName(), updatedBy.getLastName());
        }

        return new AgentDetailsRes(agent.getAgentId(), agent.getFirstName(), agent.getLastName(),
                Utils.getFullName(agent.getFirstName(), agent.getLastName()),
                Utils.getInitials(agent.getFirstName(), agent.getLastName()), agent.getAgentEmailId(),
                agent.getMobile(), agent.getRoleId(), roleName, agent.getAgentZohoUserId(),
                agent.getTicketLink(), agent.getIsProfileCompleted(), Utils.dateToString(agent.getCreatedAt()),
                Utils.dateToTime(agent.getCreatedAt()), createdByName, updatedAtDate, updatedAtTime,
                updatedByName, activities);
    }
}
