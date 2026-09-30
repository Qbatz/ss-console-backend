package com.smartstay.console.Mapper.hostelFollowUp;

import com.smartstay.console.dao.Agent;
import com.smartstay.console.dao.HostelFollowUp;
import com.smartstay.console.responses.hostelFollowUp.HostelFollowUpHistoryRes;
import com.smartstay.console.utils.Utils;

import java.util.Map;
import java.util.function.Function;

public class HostelFollowUpHistoryResMapper implements Function<HostelFollowUp, HostelFollowUpHistoryRes> {

    Map<String, Agent> agentMap;

    public HostelFollowUpHistoryResMapper(Map<String, Agent> agentMap) {
        this.agentMap = agentMap;
    }

    @Override
    public HostelFollowUpHistoryRes apply(HostelFollowUp hostelFollowUp) {

        String createdBy = null;
        String createdAtDate = null;
        String createdAtTime = null;

        if (agentMap != null) {
            if (hostelFollowUp.getCreatedBy() != null) {
                Agent createdByAgent = agentMap.getOrDefault(hostelFollowUp.getCreatedBy(), null);
                if (createdByAgent != null) {
                    createdBy = Utils.getFullName(createdByAgent.getFirstName(), createdByAgent.getLastName());
                }
            }
        }

        if (hostelFollowUp.getCreatedAt() != null) {
            createdAtDate = Utils.dateToString(hostelFollowUp.getCreatedAt());
            createdAtTime = Utils.dateToTime(hostelFollowUp.getCreatedAt());
        }

        return new HostelFollowUpHistoryRes(hostelFollowUp.getFollowUpId(), hostelFollowUp.getStatus(),
                hostelFollowUp.getComments(), hostelFollowUp.getReason(), hostelFollowUp.getCreatedBy(),
                createdBy, createdAtDate, createdAtTime);
    }
}
