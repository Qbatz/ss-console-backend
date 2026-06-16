package com.smartstay.console.Mapper.supportTicket;

import com.smartstay.console.Mapper.users.UserOwnerInfoMapper;
import com.smartstay.console.dao.*;
import com.smartstay.console.ennum.TicketStatus;
import com.smartstay.console.ennum.UserType;
import com.smartstay.console.responses.hostels.OwnerInfo;
import com.smartstay.console.responses.supportTicket.SupportTicketActivityResponse;
import com.smartstay.console.responses.supportTicket.SupportTicketResponse;
import com.smartstay.console.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class SupportTicketResMapper implements Function<SupportTicket, SupportTicketResponse> {

    Users owner;
    HostelV1 hostel;
    Map<String, Users> userByUserIdMap;
    Map<String, Agent> agentByAgentIdMap;
    List<SupportTicketActivity> activities;

    public SupportTicketResMapper(Users owner,
                                  HostelV1 hostel,
                                  Map<String, Users> userByUserIdMap,
                                  Map<String, Agent> agentByAgentIdMap,
                                  List<SupportTicketActivity> activities) {
        this.owner = owner;
        this.hostel = hostel;
        this.userByUserIdMap = userByUserIdMap;
        this.agentByAgentIdMap = agentByAgentIdMap;
        this.activities = activities;
    }

    @Override
    public SupportTicketResponse apply(SupportTicket supportTicket) {

        OwnerInfo ownerInfo = null;
        if (owner != null) {
            ownerInfo = new UserOwnerInfoMapper().apply(owner);
        }

        String hostelName = null;
        String hostelMobile = null;
        String hostelHouseNo = null;
        String hostelStreet = null;
        String hostelLandmark = null;
        String hostelCity = null;
        String hostelState = null;
        int hostelCountry = 0;
        int hostelPincode = 0;
        String fullAddress = null;
        if (hostel != null) {
            hostelName = hostel.getHostelName();
            hostelMobile = hostel.getMobile();
            hostelHouseNo = hostel.getHouseNo();
            hostelStreet = hostel.getStreet();
            hostelLandmark = hostel.getLandmark();
            hostelCity = hostel.getCity();
            hostelState = hostel.getState();
            hostelCountry = hostel.getCountry();
            hostelPincode = hostel.getPincode();
            fullAddress = Utils.buildFullAddress(hostel);
        }

        boolean isOwnerDeleted = false;
        if (supportTicket.getParentId() != null) {
            if (owner == null){
                isOwnerDeleted = true;
            }
        }

        boolean isHostelDeleted = false;
        if (supportTicket.getHostelId() != null) {
            if (hostel == null){
                isHostelDeleted = true;
            }
        }

        boolean isRaisedByDeleted = false;

        String raisedBy = null;
        String assignedTo = null;
        String assignedBy = null;
        String createdBy = null;
        if (userByUserIdMap != null && agentByAgentIdMap != null) {
            if (supportTicket.getRaisedBy() != null) {
                Users raisedByUser = userByUserIdMap.getOrDefault(supportTicket.getRaisedBy(), null);
                if (raisedByUser == null){
                    isRaisedByDeleted = true;
                } else {
                    raisedBy = Utils.getFullName(raisedByUser.getFirstName(), raisedByUser.getLastName());
                }
            }
            Agent assignedToAgent = agentByAgentIdMap.getOrDefault(supportTicket.getAssignedTo(), null);
            if (assignedToAgent != null) {
                assignedTo = Utils.getFullName(assignedToAgent.getFirstName(), assignedToAgent.getLastName());
            }
            Agent assignedByAgent = agentByAgentIdMap.getOrDefault(supportTicket.getAssignedBy(), null);
            if (assignedByAgent != null) {
                assignedBy = Utils.getFullName(assignedByAgent.getFirstName(), assignedByAgent.getLastName());
            }
            if (UserType.AGENT.name().equals(supportTicket.getCreatedByUserType())){
                Agent createdByAgent = agentByAgentIdMap.getOrDefault(supportTicket.getCreatedBy(), null);
                if (createdByAgent != null) {
                    createdBy = Utils.getFullName(createdByAgent.getFirstName(), createdByAgent.getLastName());
                }
            } else {
                Users createdByUser = userByUserIdMap.getOrDefault(supportTicket.getCreatedBy(), null);
                if (createdByUser != null) {
                    createdBy = Utils.getFullName(createdByUser.getFirstName(), createdByUser.getLastName());
                }
            }
        }

        TicketStatus currentStatus;
        try {
            currentStatus = TicketStatus.valueOf(supportTicket.getTicketStatus());
        } catch (Exception e){
            currentStatus = null;
        }

        boolean canAssignStaff = false;
        if (currentStatus != null) {
            if (currentStatus.canMoveTo(TicketStatus.ASSIGNED)){
                canAssignStaff = true;
            }
        }

        String paymentProofFileName = null;
        if (supportTicket.getPaymentProof() != null){
            paymentProofFileName = Utils.getBaseNameFromUrl(supportTicket.getPaymentProof());
        }

        List<SupportTicketActivityResponse> activitiesRes = new ArrayList<>();
        if (activities != null){
            activitiesRes = activities.stream()
                    .map(activity -> {
                        String activityCreatedBy = null;
                        if (userByUserIdMap != null && agentByAgentIdMap != null) {
                            if (UserType.AGENT.name().equals(activity.getCreatedByUserType())){
                                Agent createdByAgent = agentByAgentIdMap.getOrDefault(activity.getCreatedBy(), null);
                                if (createdByAgent != null) {
                                    activityCreatedBy = Utils.getFullName(createdByAgent.getFirstName(), createdByAgent.getLastName());
                                }
                            } else {
                                Users createdByUser = userByUserIdMap.getOrDefault(activity.getCreatedBy(), null);
                                if (createdByUser != null) {
                                    activityCreatedBy = Utils.getFullName(createdByUser.getFirstName(), createdByUser.getLastName());
                                }
                            }
                        }
                        return new SupportTicketActivityResponse(
                                activity.getActivityId(), activity.getComment(), activity.getDescription(),
                                activity.getStatus(), activity.getCreatedByUserType(), activity.getCreatedBy(),
                                activityCreatedBy, Utils.dateToString(activity.getCreatedAt()),
                                Utils.dateToTime(activity.getCreatedAt())
                        );
                    }).toList();
        }

        return new SupportTicketResponse(supportTicket.getTicketId(), supportTicket.getTicketNumber(),
                canAssignStaff, isOwnerDeleted, isHostelDeleted, isRaisedByDeleted,
                supportTicket.getParentId(), ownerInfo, supportTicket.getHostelId(), hostelName,
                hostelMobile, hostelHouseNo, hostelStreet, hostelLandmark, hostelCity, hostelState,
                hostelCountry, hostelPincode, fullAddress, supportTicket.getRaisedBy(), raisedBy,
                supportTicket.getQueryType(), supportTicket.getSubject(), supportTicket.getPriority(),
                Utils.dateToString(supportTicket.getIssueDate()), supportTicket.getAssignedTo(),
                assignedTo, supportTicket.getAssignedBy(), assignedBy, supportTicket.getRemarks(),
                supportTicket.getPaymentProof(), paymentProofFileName, supportTicket.getSource(),
                supportTicket.getTicketStatus(), supportTicket.getCreatedByUserType(),
                supportTicket.getCreatedBy(), createdBy, Utils.dateToString(supportTicket.getCreatedAt()),
                Utils.dateToTime(supportTicket.getCreatedAt()), activitiesRes);
    }
}
