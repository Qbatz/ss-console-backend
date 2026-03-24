package com.smartstay.console.Mapper.agent;

import com.smartstay.console.dao.Agent;
import com.smartstay.console.dao.AgentActivities;
import com.smartstay.console.responses.agents.AgentActivitiesRes;
import com.smartstay.console.utils.Utils;

import java.util.function.Function;

public class AgentActivitiesResMapper implements Function<AgentActivities, AgentActivitiesRes> {

    Agent agent;

    public AgentActivitiesResMapper(Agent agent) {
        this.agent = agent;
    }

    @Override
    public AgentActivitiesRes apply(AgentActivities activity) {

        String agentName = null;
        if (agent != null) {
            agentName = Utils.getFullName(agent.getFirstName(), agent.getLastName());
        }

        return new AgentActivitiesRes(activity.getActivityId(), activity.getAgentId(),
                agentName, activity.getActivityType(), activity.getDescription(), activity.getSource(),
                activity.getSourceId(), Utils.dateToString(activity.getCreatedAt()),
                Utils.dateToTime(activity.getCreatedAt()));
    }
}
