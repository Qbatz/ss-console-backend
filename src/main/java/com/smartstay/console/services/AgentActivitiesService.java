package com.smartstay.console.services;

import com.smartstay.console.dao.Agent;
import com.smartstay.console.dao.AgentActivities;
import com.smartstay.console.ennum.ActivityType;
import com.smartstay.console.ennum.Source;
import com.smartstay.console.repositories.AgentActivitiesRepository;
import com.smartstay.console.utils.AgentActivityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class AgentActivitiesService {

    @Autowired
    private AgentActivitiesRepository agentActivitiesRepository;

    public void createAgentActivity(Agent loggedInAgent,
                                    ActivityType activityType,
                                    Source source,
                                    String sourceId,
                                    Object oldObject,
                                    Object newObject){

        if (loggedInAgent == null){
            throw new IllegalArgumentException("Agent cannot be null");
        }

        if (activityType == ActivityType.CREATE && newObject == null) {
            throw new IllegalArgumentException("newObject cannot be null for CREATE");
        }

        if (activityType == ActivityType.UPDATE && (oldObject == null || newObject == null)) {
            throw new IllegalArgumentException("oldObject and newObject required for UPDATE");
        }

        if (activityType == ActivityType.DELETE && oldObject == null) {
            throw new IllegalArgumentException("oldObject required for DELETE");
        }

        AgentActivities agentActivity = new AgentActivities();

        agentActivity.setAgentId(loggedInAgent.getAgentId());
        agentActivity.setActivityType(activityType.name());
        agentActivity.setDescription(AgentActivityUtil.buildDescription(activityType, source));
        agentActivity.setSource(source.name());
        agentActivity.setSourceId(sourceId);

        switch (activityType) {

            case CREATE:
                agentActivity.setOldObject(null);
                agentActivity.setNewObject(AgentActivityUtil.singleObjectMap(newObject));
                agentActivity.setChangesJson(AgentActivityUtil.changesMap(newObject, true));
                break;

            case UPDATE:
                agentActivity.setOldObject(AgentActivityUtil.singleObjectMap(oldObject));
                agentActivity.setNewObject(AgentActivityUtil.singleObjectMap(newObject));
                agentActivity.setChangesJson(AgentActivityUtil.differences(oldObject, newObject));
                break;

            case DELETE:
                agentActivity.setOldObject(AgentActivityUtil.singleObjectMap(oldObject));
                agentActivity.setNewObject(null);
                agentActivity.setChangesJson(AgentActivityUtil.changesMap(oldObject, false));
                break;

            default:
                agentActivity.setOldObject(null);
                agentActivity.setNewObject(null);
                agentActivity.setChangesJson(null);
        }

        agentActivity.setCreatedAt(new Date());

        agentActivitiesRepository.save(agentActivity);
    }
}
