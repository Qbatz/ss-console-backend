package com.smartstay.console.Mapper.demoRequests;

import com.smartstay.console.dao.Agent;
import com.smartstay.console.dao.DemoRequest;
import com.smartstay.console.responses.demoRequest.DemoRequestResponse;
import com.smartstay.console.utils.Utils;

import java.util.Map;
import java.util.function.Function;

public class DemoRequestMapper implements Function<DemoRequest, DemoRequestResponse> {

    Map<String, Agent> agentMap;

    public DemoRequestMapper(Map<String, Agent> agentMap) {
        this.agentMap = agentMap;
    }

    @Override
    public DemoRequestResponse apply(DemoRequest demoRequest) {

        String assignedTo = null;
        String assignedBy = null;
        String presentedBy = null;

        if (agentMap != null) {
            if (agentMap.get(demoRequest.getAssignedTo()) != null) {
                Agent assignedToAgent = agentMap.get(demoRequest.getAssignedTo());
                assignedTo = Utils.getFullName(assignedToAgent.getFirstName(), assignedToAgent.getLastName());
            }
            if (agentMap.get(demoRequest.getAssignedBy()) != null) {
                Agent assignedByAgent = agentMap.get(demoRequest.getAssignedBy());
                assignedBy = Utils.getFullName(assignedByAgent.getFirstName(), assignedByAgent.getLastName());
            }
            if (agentMap.get(demoRequest.getPresentedBy()) != null) {
                Agent presentedByAgent = agentMap.get(demoRequest.getPresentedBy());
                presentedBy = Utils.getFullName(presentedByAgent.getFirstName(), presentedByAgent.getLastName());
            }
        }

        return new DemoRequestResponse(demoRequest.getRequestId(), demoRequest.getName(),
                demoRequest.getEmailId(), demoRequest.getContactNo(), demoRequest.getCountryCode(),
                demoRequest.getOrganization(), demoRequest.getNoOfHostels(), demoRequest.getNoOfTenant(),
                demoRequest.getCity(), demoRequest.getState(), demoRequest.getCountry(), demoRequest.getDemoRequestStatus(),
                demoRequest.getIsDemoCompleted(), demoRequest.getIsAssigned(), assignedTo, assignedBy,
                presentedBy, demoRequest.getComments(), demoRequest.getRequestedDate(), demoRequest.getRequestedTime(),
                Utils.dateToString(demoRequest.getPresentedAt()), Utils.dateToTime(demoRequest.getPresentedAt()));
    }
}
