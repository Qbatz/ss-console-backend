package com.smartstay.console.Mapper.supportTicket;

import com.smartstay.console.Mapper.users.UserOwnerInfoMapper;
import com.smartstay.console.dao.Agent;
import com.smartstay.console.dao.HostelV1;
import com.smartstay.console.dao.SupportTicket;
import com.smartstay.console.dao.Users;
import com.smartstay.console.ennum.TicketStatus;
import com.smartstay.console.ennum.UserType;
import com.smartstay.console.responses.hostels.OwnerInfo;
import com.smartstay.console.responses.supportTicket.SupportTicketListResponse;
import com.smartstay.console.utils.Utils;

import java.util.Map;
import java.util.function.Function;

public class SupportTicketListResMapper implements Function<SupportTicket, SupportTicketListResponse> {

    Users owner;
    HostelV1 hostel;
    Map<String, Users> userByUserIdMap;
    Map<String, Agent> agentByAgentIdMap;

    public SupportTicketListResMapper(Users owner,
                                      HostelV1 hostel,
                                      Map<String, Users> userByUserIdMap,
                                      Map<String, Agent> agentByAgentIdMap) {
        this.owner = owner;
        this.hostel = hostel;
        this.userByUserIdMap = userByUserIdMap;
        this.agentByAgentIdMap = agentByAgentIdMap;
    }

    @Override
    public SupportTicketListResponse apply(SupportTicket supportTicket) {

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
        boolean isReAssignStaff = false;
        if (currentStatus != null) {
            if (currentStatus.canMoveTo(TicketStatus.ASSIGNED)){
                canAssignStaff = true;
                if (assignedTo != null){
                    isReAssignStaff = true;
                }
            }
        }

        String paymentProofFileName = null;
        if (supportTicket.getPaymentProof() != null){
            paymentProofFileName = Utils.getBaseNameFromUrl(supportTicket.getPaymentProof());
        }

        return new SupportTicketListResponse(supportTicket.getTicketId(), supportTicket.getTicketNumber(),
                canAssignStaff, isReAssignStaff, isOwnerDeleted, isHostelDeleted, isRaisedByDeleted,
                supportTicket.getParentId(), ownerInfo, supportTicket.getHostelId(), hostelName,
                hostelMobile, hostelHouseNo, hostelStreet, hostelLandmark, hostelCity, hostelState,
                hostelCountry, hostelPincode, fullAddress, supportTicket.getRaisedBy(), raisedBy,
                supportTicket.getQueryType(), supportTicket.getSubject(), supportTicket.getPriority(),
                Utils.dateToString(supportTicket.getIssueDate()), supportTicket.getAssignedTo(), assignedTo,
                supportTicket.getAssignedBy(), assignedBy, supportTicket.getRemarks(), supportTicket.getPaymentProof(),
                paymentProofFileName, supportTicket.getSource(), supportTicket.getTicketStatus(),
                supportTicket.getCreatedByUserType(), supportTicket.getCreatedBy(), createdBy,
                Utils.dateToString(supportTicket.getCreatedAt()), Utils.dateToTime(supportTicket.getCreatedAt()));
    }
}
