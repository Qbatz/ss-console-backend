package com.smartstay.console.Mapper.hostels;

import com.smartstay.console.dao.Agent;
import com.smartstay.console.dao.HostelV1;
import com.smartstay.console.dao.RecurringTracker;
import com.smartstay.console.responses.hostels.RecurringTrackerRes;
import com.smartstay.console.utils.Utils;

import java.util.Map;
import java.util.function.Function;

public class RecurringTrackerResMapper implements Function<RecurringTracker, RecurringTrackerRes> {

    HostelV1 hostel;
    Map<String, Agent> agentMap;

    public RecurringTrackerResMapper(HostelV1 hostel,
                                     Map<String, Agent> agentMap) {
        this.hostel = hostel;
        this.agentMap = agentMap;
    }

    @Override
    public RecurringTrackerRes apply(RecurringTracker recurringTracker) {

        String hostelName = null;
        if(hostel != null){
            hostelName = hostel.getHostelName();
        }

        String createdBy = null;
        if (agentMap != null){
            Agent agent = agentMap.get(recurringTracker.getCreatedBy());
            if (agent != null){
                createdBy = Utils.getFullName(agent.getFirstName(), agent.getLastName());
            }
        }

        return new RecurringTrackerRes(recurringTracker.getTrackerId(), hostelName, recurringTracker.getCreationDay(),
                recurringTracker.getCreationMonth(), recurringTracker.getCreationYear(), recurringTracker.getMode(),
                createdBy, Utils.dateToString(recurringTracker.getCreatedAt()), Utils.dateToTime(recurringTracker.getCreatedAt()));
    }
}
