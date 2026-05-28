package com.smartstay.console.Mapper.demoRequests;

import com.smartstay.console.Mapper.users.UserOwnerInfoMapper;
import com.smartstay.console.dao.*;
import com.smartstay.console.ennum.DemoRequestStatus;
import com.smartstay.console.responses.demoRequest.DemoRequestActivityResponse;
import com.smartstay.console.responses.demoRequest.DemoRequestCommentsResponse;
import com.smartstay.console.responses.demoRequest.DemoRequestResponse;
import com.smartstay.console.responses.hostels.OwnerInfo;
import com.smartstay.console.utils.Utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class DemoRequestMapper implements Function<DemoRequest, DemoRequestResponse> {

    Map<String, Agent> agentMap;
    Users owner;
    List<DemoRequestComments> comments;
    List<DemoRequestActivity> activities;

    public DemoRequestMapper(Map<String, Agent> agentMap,
                             Users owner,
                             List<DemoRequestComments> comments,
                             List<DemoRequestActivity> activities) {
        this.agentMap = agentMap;
        this.owner = owner;
        this.comments = comments;
        this.activities = activities;
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

        String requestedDate = null;
        String requestedTime = null;
        if (demoRequest.getBookedFor() != null) {
            requestedDate = Utils.dateToString(demoRequest.getBookedFor());
            requestedTime = Utils.dateToTime(demoRequest.getBookedFor());
        } else if (demoRequest.getRequestedDate() != null) {
            requestedDate = Utils.formatDateString(demoRequest.getRequestedDate());
            requestedTime = demoRequest.getRequestedTime();
        }

        if (requestedTime == null){
            requestedTime = demoRequest.getRequestedTime();
        }

        List<DemoRequestCommentsResponse> demoRequestComments = new ArrayList<>();
        if (comments != null) {
            demoRequestComments = comments.stream()
                    .sorted(Comparator.comparing(DemoRequestComments::getId).reversed())
                    .map(comment -> {
                        String commentCreatedBy = null;
                        if (agentMap != null) {
                            if (agentMap.get(comment.getCreatedBy()) != null) {
                                Agent commentCreatedByAgent = agentMap.get(comment.getCreatedBy());
                                commentCreatedBy = Utils.getFullName(commentCreatedByAgent.getFirstName(), commentCreatedByAgent.getLastName());
                            }
                        }
                        return new DemoRequestCommentsResponse(comment.getId(), comment.getComment(), comment.getCreatedByUserType(),
                                commentCreatedBy, Utils.dateToString(comment.getCreatedAt()), Utils.dateToTime(comment.getCreatedAt()));
                    }).toList();
        }

        List<DemoRequestActivityResponse> demoRequestActivities = new ArrayList<>();
        if (activities != null) {
            demoRequestActivities = activities.stream()
                    .sorted(Comparator.comparing(DemoRequestActivity::getActivityId).reversed())
                    .map(activity -> {
                        String activityCreatedBy = null;
                        if (agentMap != null) {
                            if (agentMap.get(activity.getCreatedBy()) != null) {
                                Agent activityCreatedByAgent = agentMap.get(activity.getCreatedBy());
                                activityCreatedBy = Utils.getFullName(activityCreatedByAgent.getFirstName(),
                                        activityCreatedByAgent.getLastName());
                            }
                        }
                        return new DemoRequestActivityResponse(activity.getActivityId(), activity.getComment(),
                                activity.getDescription(), activity.getStatus(), activity.getCreatedByUserType(),
                                activityCreatedBy, Utils.dateToString(activity.getCreatedAt()),
                                Utils.dateToTime(activity.getCreatedAt()));
                    }).toList();
        }

        DemoRequestStatus currentStatus;
        try {
            currentStatus = DemoRequestStatus.valueOf(demoRequest.getDemoRequestStatus());
        } catch (Exception e){
            currentStatus = null;
        }

        boolean canAssignStaff = false;
        boolean canMarkDropped = false;
        if (currentStatus != null) {
            if (currentStatus.canMoveTo(DemoRequestStatus.ASSIGNED)){
                canAssignStaff = true;
            }
            if (currentStatus.canMoveTo(DemoRequestStatus.DROPPED)){
                canMarkDropped = true;
            }
        }

        OwnerInfo ownerInfo = null;
        if (owner != null) {
            ownerInfo = new UserOwnerInfoMapper().apply(owner);
        }

        return new DemoRequestResponse(demoRequest.getRequestId(), demoRequest.getName(),
                demoRequest.getEmailId(), demoRequest.getContactNo(), demoRequest.getCountryCode(),
                demoRequest.getOrganization(), demoRequest.getNoOfHostels(), demoRequest.getNoOfTenant(),
                demoRequest.getCity(), demoRequest.getState(), demoRequest.getCountry(), demoRequest.getDemoRequestStatus(),
                canAssignStaff, canMarkDropped, demoRequest.getIsDemoCompleted(), demoRequest.getIsAssigned(),
                assignedTo, assignedBy, presentedBy, demoRequest.getComments(), requestedDate, requestedTime,
                demoRequest.getPresentedAt() != null ? Utils.dateToString(demoRequest.getPresentedAt()) :  null,
                demoRequest.getPresentedAt() != null ? Utils.dateToTime(demoRequest.getPresentedAt()) : null,
                demoRequest.getSource(), demoRequest.getParentId(), ownerInfo,
                demoRequest.getDemoDateFrom() != null ? Utils.dateToString(demoRequest.getDemoDateFrom()) : null,
                demoRequest.getDemoDateFrom() != null ? Utils.dateToTime(demoRequest.getDemoDateFrom()) : null,
                demoRequest.getDemoDateTo() != null ? Utils.dateToTime(demoRequest.getDemoDateTo()) : null,
                demoRequest.getDemoType(), demoRequest.getDemoMeetLink(),
                demoRequest.getDropReason(), Utils.dateToString(demoRequest.getCreatedAt()),
                Utils.dateToTime(demoRequest.getCreatedAt()), demoRequestComments, demoRequestActivities);
    }
}
