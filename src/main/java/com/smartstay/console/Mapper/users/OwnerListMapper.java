package com.smartstay.console.Mapper.users;

import com.smartstay.console.dao.Agent;
import com.smartstay.console.dao.HostelRelationalAgent;
import com.smartstay.console.dao.UserActivities;
import com.smartstay.console.dto.users.OwnerWithAddressProjection;
import com.smartstay.console.responses.hostelRelationalAgent.HostelRelationalAgentResponse;
import com.smartstay.console.responses.users.AddressResponse;
import com.smartstay.console.responses.users.OwnerResponse;
import com.smartstay.console.utils.Utils;

import java.util.*;
import java.util.function.Function;

public class OwnerListMapper implements Function<OwnerWithAddressProjection, OwnerResponse> {

    int noOfProperties;
    UserActivities userActivities;
    List<HostelRelationalAgent> relationalAgents;
    Map<String, Agent> agentMap;

    public OwnerListMapper(int noOfProperties,
                           UserActivities userActivities,
                           List<HostelRelationalAgent> relationalAgents,
                           Map<String, Agent> agentMap) {
        this.noOfProperties = noOfProperties;
        this.userActivities = userActivities;
        this.relationalAgents = relationalAgents;
        this.agentMap = agentMap;
    }

    @Override
    public OwnerResponse apply(OwnerWithAddressProjection owner) {

        String fullName = Utils.getFullName(owner.getFirstName(), owner.getLastName());
        String initials = Utils.getInitials(owner.getFirstName(), owner.getLastName());

        AddressResponse addressRes = null;

        if (owner.getAddressId() != null){
            addressRes = new AddressResponse(owner.getAddressId(), owner.getHouseNo(), owner.getStreet(),
                    owner.getLandMark(), owner.getCity(), owner.getState(), owner.getPincode());
        }

        Date latestActivityDate = null;
        if (userActivities != null){
            latestActivityDate = userActivities.getCreatedAt();
        }

        List<HostelRelationalAgentResponse> relationalAgentResponses = new ArrayList<>();
        if (relationalAgents != null) {
            relationalAgentResponses = relationalAgents.stream()
                    .sorted(Comparator.comparing(HostelRelationalAgent::getId).reversed())
                    .map(hostelRelationalAgent -> {
                        Agent relationalAgent = agentMap.getOrDefault(hostelRelationalAgent.getAgentId(), null);
                        Agent createdByAgent = agentMap.getOrDefault(hostelRelationalAgent.getCreatedBy(), null);

                        String relationalAgentName = null;
                        if (relationalAgent != null){
                            relationalAgentName = Utils.getFullName(relationalAgent.getFirstName(), relationalAgent.getLastName());
                        }

                        String createdByAgentName = null;
                        if (createdByAgent != null){
                            createdByAgentName = Utils.getFullName(createdByAgent.getFirstName(), createdByAgent.getLastName());
                        }

                        return new HostelRelationalAgentResponse(hostelRelationalAgent.getId(), hostelRelationalAgent.getParentId(),
                                hostelRelationalAgent.getAgentId(), relationalAgentName, hostelRelationalAgent.getReason().name(),
                                hostelRelationalAgent.getComments(), createdByAgentName, Utils.dateToString(hostelRelationalAgent.getCreatedAt()),
                                Utils.dateToTime(hostelRelationalAgent.getCreatedAt()));
                    }).toList();
        }

        return new OwnerResponse(owner.getUserId(), owner.getParentId(), owner.getFirstName(),
                owner.getLastName(), fullName, initials, owner.getMobileNo(), owner.getEmailId(),
                noOfProperties, addressRes, Utils.dateToString(owner.getCreatedAt()),
                latestActivityDate != null ? Utils.dateToString(latestActivityDate) : null,
                latestActivityDate != null ? Utils.dateToTime(latestActivityDate) : null,
                relationalAgentResponses
        );
    }
}
