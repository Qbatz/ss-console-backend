package com.smartstay.console.Mapper.hostelFollowUp;

import com.smartstay.console.dao.Agent;
import com.smartstay.console.dao.HostelFollowUp;
import com.smartstay.console.responses.hostelFollowUp.HostelFollowUpHistoryRes;
import com.smartstay.console.responses.hostelFollowUp.HostelFollowUpResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class HostelFollowUpResMapper implements Function<HostelFollowUp, HostelFollowUpResponse> {

    List<HostelFollowUp> hostelFollowUps;
    Map<String, Agent> agentMap;

    public HostelFollowUpResMapper(List<HostelFollowUp> hostelFollowUps,
                                   Map<String, Agent> agentMap) {
        this.hostelFollowUps = hostelFollowUps;
        this.agentMap = agentMap;
    }

    @Override
    public HostelFollowUpResponse apply(HostelFollowUp hostelFollowUp) {

        HostelFollowUpHistoryResMapper mapper = new HostelFollowUpHistoryResMapper(agentMap);

        List<HostelFollowUpHistoryRes> followUpHistory = new ArrayList<>();

        if (hostelFollowUps != null && !hostelFollowUps.isEmpty()) {

            followUpHistory = hostelFollowUps.stream()
                    .map(mapper)
                    .toList();
        }

        return new HostelFollowUpResponse(hostelFollowUp.getFollowUpId(), hostelFollowUp.getStatus(),
                hostelFollowUp.getComments(), hostelFollowUp.getReason(), followUpHistory);
    }
}
