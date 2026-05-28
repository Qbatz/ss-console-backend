package com.smartstay.console.services;

import com.smartstay.console.Mapper.demoRequests.DemoRequestMapper;
import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.*;
import com.smartstay.console.dto.demoRequest.DemoRequestCommentsSnapshot;
import com.smartstay.console.dto.demoRequest.DemoRequestSnapshot;
import com.smartstay.console.dto.demoRequest.DemoRequestStatsProjection;
import com.smartstay.console.ennum.*;
import com.smartstay.console.payloads.demoRequest.*;
import com.smartstay.console.repositories.DemoRequestRepository;
import com.smartstay.console.responses.demoRequest.*;
import com.smartstay.console.utils.SnapshotUtility;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DemoRequestService {

    @Autowired
    private Authentication authentication;
    @Autowired
    private DemoRequestRepository demoRequestRepository;
    @Autowired
    private AgentService agentService;
    @Autowired
    private AgentActivitiesService agentActivitiesService;
    @Autowired
    private DemoRequestCommentsService demoRequestCommentsService;
    @Autowired
    private DemoRequestActivityService demoRequestActivityService;
    @Autowired
    private UsersService usersService;

    public ResponseEntity<?> addDemoRequest(DemoRequestPayload demoRequestPayload) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!Utils.checkDateIsFromFutureOrPresent(demoRequestPayload.requestedDate(), demoRequestPayload.requestedTime())) {
            return new ResponseEntity<>(Utils.DATE_IS_NOT_FROM_FUTURE_OR_PRESENT, HttpStatus.BAD_REQUEST);
        }

        Date today = new Date();

        DemoRequest demoRequest = new DemoRequest();
        demoRequest.setName(demoRequestPayload.name());
        demoRequest.setEmailId(demoRequestPayload.email());
        demoRequest.setContactNo(demoRequestPayload.contactNo());
        demoRequest.setCountryCode(demoRequestPayload.countryCode());
        demoRequest.setOrganization(demoRequestPayload.organization());
        demoRequest.setNoOfHostels(demoRequestPayload.noOfHostels());
        demoRequest.setNoOfTenant(demoRequestPayload.noOfTenants());
        demoRequest.setCity(demoRequestPayload.city());
        demoRequest.setState(demoRequestPayload.state());
        demoRequest.setCountry(demoRequestPayload.country());
        demoRequest.setDemoRequestStatus(DemoRequestStatus.NEW.name());
        demoRequest.setIsDemoCompleted(false);
        demoRequest.setIsAssigned(false);
        demoRequest.setComments(demoRequestPayload.comments());
        demoRequest.setBookedFor(Utils
                .localDateTimeToDate(demoRequestPayload.requestedDate(), demoRequestPayload.requestedTime()));
        demoRequest.setRequestedDate(Utils.localDateToString(demoRequestPayload.requestedDate()));
        demoRequest.setRequestedTime(Utils.localTimeToString(demoRequestPayload.requestedTime()));
        demoRequest.setSource(DemoRequestSource.CONSOLE.name());
        demoRequest.setCreatedAt(today);

        demoRequest = demoRequestRepository.save(demoRequest);

        DemoRequestActivity demoRequestActivity = new DemoRequestActivity();
        demoRequestActivity.setComment(demoRequestPayload.comments());
        demoRequestActivity.setDescription(DemoRequestStatus.NEW.getDescription());
        demoRequestActivity.setStatus(DemoRequestStatus.NEW.name());
        demoRequestActivity.setCreatedByUserType(UserType.AGENT.name());
        demoRequestActivity.setCreatedBy(authentication.getName());
        demoRequestActivity.setCreatedAt(today);
        demoRequestActivity.setRequestId(demoRequest.getRequestId());

        demoRequestActivity = demoRequestActivityService.save(demoRequestActivity);

        agentActivitiesService.createAgentActivity(agent, ActivityType.CREATE, Source.DEMO_REQUEST,
                String.valueOf(demoRequest.getRequestId()), null, demoRequest);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ResponseEntity<?> getAllDemoRequests(int page, int size, String name,
                                                Date startDate, Date endDate,
                                                String status, String agentId) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        LocalDate today = LocalDate.now();
        Date now = new Date();
        now = Utils.getStartOfDay(now);
        Date nowEnds = Utils.addDaysToDate(now, 1);

        Date currentMonthStartDate = Utils.getStartDateOfMonth(today);
        Date currentMonthEndDate = Utils.getEndDateOfMonth(today);
        Date currentMonthEndDatePlus1 = Utils.addDaysToDate(currentMonthEndDate, 1);

        DemoRequestStatsProjection stats = demoRequestRepository.getDashboardStats(
                now,
                nowEnds,
                currentMonthStartDate,
                currentMonthEndDatePlus1
        );

        long totalLeads = stats.getTotalLeads();
        long todayNewCount = stats.getTodayNewCount();
        long newCount = stats.getNewCount();
        long assignedCount = stats.getAssignedCount();
        long contactedCount = stats.getContactedCount();
        long demoScheduledCount = stats.getDemoScheduledCount();
        long demoCompletedCount = stats.getDemoCompletedCount();
        long trialStartedCount = stats.getTrialStartedCount();
        long convertedCount = stats.getConvertedCount();
        long droppedCount = stats.getDroppedCount();

        if (startDate == null) {
            startDate = currentMonthStartDate;
        }
        if (endDate == null) {
            endDate = currentMonthEndDate;
        }
        endDate = Utils.addDaysToDate(endDate, 1);

        page = Math.max(page - 1, 0);
        size = Math.max(size, 1);

        name = name == null || name.isBlank() ? null : name.trim();
        status = (status == null || status.isBlank()) ? null : status.trim();
        agentId = (agentId == null || agentId.isBlank()) ? null : agentId.trim();

        Pageable pageable = PageRequest.of(page, size);

        Page<DemoRequest> paginatedDemoRequest = demoRequestRepository
                .findAllPaginated(name, startDate, endDate, status, agentId, pageable);

        List<DemoRequest> demoRequests = paginatedDemoRequest.getContent();

        Set<Long> demoRequestIds = demoRequests.stream()
                .map(DemoRequest::getRequestId)
                .collect(Collectors.toSet());

        List<DemoRequestComments> demoRequestComments = demoRequestCommentsService
                .getDemoRequestCommentsByRequestIds(demoRequestIds);
        Map<Long, List<DemoRequestComments>> demoRequestCommentsMap = demoRequestComments.stream()
                .collect(Collectors.groupingBy(DemoRequestComments::getRequestId));

        List<DemoRequestActivity> demoRequestActivities = demoRequestActivityService
                .getDemoRequestActivitiesByRequestIds(demoRequestIds);
        Map<Long, List<DemoRequestActivity>> demoRequestActivitiesMap = demoRequestActivities.stream()
                .collect(Collectors.groupingBy(DemoRequestActivity::getRequestId));

        Set<String> assignedToIds = new HashSet<>();
        Set<String> assignedByIds = new HashSet<>();
        Set<String> presentedByIds = new HashSet<>();
        Set<String> commentCreatedByIds = new HashSet<>();
        Set<String> activityCreatedByIds = new HashSet<>();
        Set<String> parentIds = new HashSet<>();

        for (DemoRequest demoRequest : demoRequests) {
            assignedToIds.add(demoRequest.getAssignedTo());
            assignedByIds.add(demoRequest.getAssignedBy());
            presentedByIds.add(demoRequest.getPresentedBy());

            parentIds.add(demoRequest.getParentId());

            List<DemoRequestComments> comments = demoRequestCommentsMap
                    .getOrDefault(demoRequest.getRequestId(), null);
            if (comments != null){
                for (DemoRequestComments demoRequestComment : comments) {
                    if (UserType.AGENT.name().equals(demoRequestComment.getCreatedByUserType())){
                        commentCreatedByIds.add(demoRequestComment.getCreatedBy());
                    }
                }
            }

            List<DemoRequestActivity> activities = demoRequestActivitiesMap
                    .getOrDefault(demoRequest.getRequestId(), null);
            if (activities != null){
                for (DemoRequestActivity demoRequestActivity : activities) {
                    if (UserType.AGENT.name().equals(demoRequestActivity.getCreatedByUserType())){
                        activityCreatedByIds.add(demoRequestActivity.getCreatedBy());
                    }
                }
            }
        }

        Set<String> agentIds = new HashSet<>();
        agentIds.addAll(assignedToIds);
        agentIds.addAll(assignedByIds);
        agentIds.addAll(presentedByIds);
        agentIds.addAll(commentCreatedByIds);
        agentIds.addAll(activityCreatedByIds);

        List<Agent> agents = agentService.getAgentsByIds(agentIds);
        Map<String, Agent> agentMap = agents.stream()
                .collect(Collectors.toMap(Agent::getAgentId, a -> a));

        List<Users> owners = usersService.getOwners(new ArrayList<>(parentIds));
        Map<String, Users> ownerMap = owners.stream()
                .collect(Collectors.toMap(Users::getParentId, a -> a));

        List<DemoRequestResponse> demoRequestList = demoRequests.stream()
                .map(demoRequest -> {
                    Users owner = ownerMap.getOrDefault(demoRequest.getParentId(), null);
                    return new DemoRequestMapper(agentMap, owner,
                            demoRequestCommentsMap.getOrDefault(demoRequest.getRequestId(), null),
                            demoRequestActivitiesMap.getOrDefault(demoRequest.getRequestId(), null))
                            .apply(demoRequest);
                })
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("demoRequestList", demoRequestList);
        response.put("currentPage", page + 1);
        response.put("pageSize", size);
        response.put("totalItems", paginatedDemoRequest.getTotalElements());
        response.put("totalPages", paginatedDemoRequest.getTotalPages());
        response.put("totalLeads", totalLeads);
        response.put("newToday", todayNewCount);
        response.put("new", newCount);
        response.put("assigned", assignedCount);
        response.put("contacted", contactedCount);
        response.put("demoScheduled", demoScheduledCount);
        response.put("demoCompleted", demoCompletedCount);
        response.put("trialStarted", trialStartedCount);
        response.put("converted", convertedCount);
        response.put("dropped", droppedCount);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> getDemoRequest(Long demoRequestId) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        DemoRequest demoRequest = demoRequestRepository.findByRequestId(demoRequestId);
        if (demoRequest == null) {
            return new ResponseEntity<>(Utils.DEMO_REQUEST_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        Set<String> commentCreatedByIds = new HashSet<>();
        Set<String> activityCreatedByIds = new HashSet<>();

        List<DemoRequestComments> demoRequestComments = demoRequestCommentsService
                .getDemoRequestCommentsByRequestId(demoRequest.getRequestId());
        if (demoRequestComments != null){
            for (DemoRequestComments demoRequestComment : demoRequestComments) {
                if (UserType.AGENT.name().equals(demoRequestComment.getCreatedByUserType())){
                    commentCreatedByIds.add(demoRequestComment.getCreatedBy());
                }
            }
        }

        List<DemoRequestActivity> demoRequestActivities = demoRequestActivityService
                .getDemoRequestActivitiesByRequestId(demoRequest.getRequestId());
        if (demoRequestActivities != null){
            for (DemoRequestActivity demoRequestActivity : demoRequestActivities) {
                if (UserType.AGENT.name().equals(demoRequestActivity.getCreatedByUserType())){
                    activityCreatedByIds.add(demoRequestActivity.getCreatedBy());
                }
            }
        }

        Set<String> agentIds = new HashSet<>();
        agentIds.add(demoRequest.getAssignedTo());
        agentIds.add(demoRequest.getAssignedBy());
        agentIds.add(demoRequest.getPresentedBy());
        agentIds.addAll(commentCreatedByIds);
        agentIds.addAll(activityCreatedByIds);

        List<Agent> agents = agentService.getAgentsByIds(agentIds);
        Map<String, Agent> agentMap = agents.stream()
                .collect(Collectors.toMap(Agent::getAgentId, a -> a));

        Users owner = usersService.getOwner(demoRequest.getParentId());

        DemoRequestResponse response = new DemoRequestMapper(agentMap, owner, demoRequestComments,
                demoRequestActivities).apply(demoRequest);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> assignDemoRequest(Long demoRequestId, DemoRequestAssignPayload payload) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        DemoRequest demoRequest = demoRequestRepository.findByRequestId(demoRequestId);
        if (demoRequest == null){
            return new ResponseEntity<>(Utils.DEMO_REQUEST_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        DemoRequestStatus currentStatus;
        try {
            currentStatus = DemoRequestStatus.valueOf(
                    demoRequest.getDemoRequestStatus()
            );
        } catch (IllegalArgumentException | NullPointerException e) {
            return new ResponseEntity<>(Utils.DEMO_REQUEST_STATUS_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        if (!currentStatus.canMoveTo(DemoRequestStatus.ASSIGNED)) {
            return new ResponseEntity<>(Utils.INVALID_STATUS_TRANSITION, HttpStatus.BAD_REQUEST);
        }

        DemoRequestSnapshot oldRequest = SnapshotUtility.toSnapshot(demoRequest);

        Agent assignedTo = agentService.findUserByUserId(payload.agentId());
        if (assignedTo == null) {
            return new ResponseEntity<>(Utils.NO_AGENT_FOUND, HttpStatus.BAD_REQUEST);
        }

        demoRequest.setDemoRequestStatus(DemoRequestStatus.ASSIGNED.name());
        demoRequest.setIsAssigned(true);
        demoRequest.setAssignedTo(assignedTo.getAgentId());
        demoRequest.setAssignedBy(agent.getAgentId());
        demoRequest.setComments(payload.comments());

        demoRequest = demoRequestRepository.save(demoRequest);

        DemoRequestActivity demoRequestActivity = new DemoRequestActivity();
        demoRequestActivity.setComment(payload.comments());
        demoRequestActivity.setDescription(DemoRequestStatus.ASSIGNED.getDescription());
        demoRequestActivity.setStatus(DemoRequestStatus.ASSIGNED.name());
        demoRequestActivity.setCreatedByUserType(UserType.AGENT.name());
        demoRequestActivity.setCreatedBy(authentication.getName());
        demoRequestActivity.setCreatedAt(new Date());
        demoRequestActivity.setRequestId(demoRequest.getRequestId());

        demoRequestActivity = demoRequestActivityService.save(demoRequestActivity);

        DemoRequestSnapshot newRequest = SnapshotUtility.toSnapshot(demoRequest);

        agentActivitiesService.createAgentActivity(agent, ActivityType.UPDATE, Source.DEMO_REQUEST,
                String.valueOf(demoRequestId), oldRequest, newRequest);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ResponseEntity<?> markAsDropped(Long demoRequestId, DemoRequestDroppedPayload payload) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        DemoRequest demoRequest = demoRequestRepository.findByRequestId(demoRequestId);
        if (demoRequest == null){
            return new ResponseEntity<>(Utils.DEMO_REQUEST_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        DemoRequestStatus currentStatus;
        try {
            currentStatus = DemoRequestStatus.valueOf(
                    demoRequest.getDemoRequestStatus()
            );
        } catch (IllegalArgumentException | NullPointerException e) {
            return new ResponseEntity<>(Utils.DEMO_REQUEST_STATUS_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        if (!currentStatus.canMoveTo(DemoRequestStatus.DROPPED)) {
            return new ResponseEntity<>(Utils.INVALID_STATUS_TRANSITION, HttpStatus.BAD_REQUEST);
        }

        DemoRequestSnapshot oldRequest = SnapshotUtility.toSnapshot(demoRequest);

        DropReason dropReason;
        try {
            dropReason = DropReason.valueOf(payload.dropReason());
        } catch (IllegalArgumentException | NullPointerException e) {
            return new ResponseEntity<>(Utils.DROP_REASON_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        demoRequest.setDemoRequestStatus(DemoRequestStatus.DROPPED.name());
        demoRequest.setDropReason(dropReason.name());
        demoRequest.setComments(payload.comments());

        demoRequest = demoRequestRepository.save(demoRequest);

        DemoRequestActivity demoRequestActivity = new DemoRequestActivity();
        demoRequestActivity.setComment(payload.comments());
        demoRequestActivity.setDescription(DemoRequestStatus.DROPPED.getDescription());
        demoRequestActivity.setStatus(DemoRequestStatus.DROPPED.name());
        demoRequestActivity.setCreatedByUserType(UserType.AGENT.name());
        demoRequestActivity.setCreatedBy(authentication.getName());
        demoRequestActivity.setCreatedAt(new Date());
        demoRequestActivity.setRequestId(demoRequest.getRequestId());

        demoRequestActivity = demoRequestActivityService.save(demoRequestActivity);

        DemoRequestSnapshot newRequest = SnapshotUtility.toSnapshot(demoRequest);

        agentActivitiesService.createAgentActivity(agent, ActivityType.UPDATE, Source.DEMO_REQUEST,
                String.valueOf(demoRequestId), oldRequest, newRequest);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ResponseEntity<?> updateDemoRequestStatus(Long demoRequestId,
                                                     DemoRequestStatusPayload demoRequestStatusPayload) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        DemoRequest demoRequest = demoRequestRepository.findByRequestId(demoRequestId);
        if (demoRequest == null){
            return new ResponseEntity<>(Utils.DEMO_REQUEST_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        DemoRequestSnapshot oldRequest = SnapshotUtility.toSnapshot(demoRequest);

        DemoRequestStatus requestStatus;
        try {
            requestStatus = DemoRequestStatus.valueOf(
                    demoRequestStatusPayload.demoRequestStatus().toUpperCase()
            );
        } catch (IllegalArgumentException | NullPointerException e) {
            return new ResponseEntity<>(Utils.DEMO_REQUEST_STATUS_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        DemoRequestStatus currentStatus;
        try {
            currentStatus = DemoRequestStatus.valueOf(
                    demoRequest.getDemoRequestStatus()
            );
        } catch (IllegalArgumentException | NullPointerException e) {
            return new ResponseEntity<>(Utils.DEMO_REQUEST_STATUS_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        if (!currentStatus.canMoveTo(requestStatus)) {
            return new ResponseEntity<>(Utils.INVALID_STATUS_TRANSITION, HttpStatus.BAD_REQUEST);
        }

        demoRequest.setDemoRequestStatus(requestStatus.name());

        if (demoRequestStatusPayload.comments() != null && !demoRequestStatusPayload.comments().isBlank()){
            demoRequest.setComments(demoRequestStatusPayload.comments());
        }

        if (requestStatus.name().equals(DemoRequestStatus.ASSIGNED.name())){

            if (demoRequestStatusPayload.agentId() == null || demoRequestStatusPayload.agentId().isBlank()){
                return new ResponseEntity<>(Utils.AGENT_ID_REQUIRED, HttpStatus.BAD_REQUEST);
            }

            Agent assignedTo = agentService.findUserByUserId(demoRequestStatusPayload.agentId());
            if (assignedTo == null) {
                return new ResponseEntity<>(Utils.NO_AGENT_FOUND, HttpStatus.BAD_REQUEST);
            }

            demoRequest.setIsAssigned(true);
            demoRequest.setAssignedTo(assignedTo.getAgentId());
            demoRequest.setAssignedBy(agent.getAgentId());
        }

        if (requestStatus.name().equals(DemoRequestStatus.DEMO_SCHEDULED.name())){

            if (demoRequestStatusPayload.agentId() != null && !demoRequestStatusPayload.agentId().isBlank()){
                Agent assignedTo = agentService.findUserByUserId(demoRequestStatusPayload.agentId());
                if (assignedTo == null) {
                    return new ResponseEntity<>(Utils.NO_AGENT_FOUND, HttpStatus.BAD_REQUEST);
                }

                demoRequest.setIsAssigned(true);
                demoRequest.setAssignedTo(assignedTo.getAgentId());
                demoRequest.setAssignedBy(agent.getAgentId());
            }

            if (demoRequestStatusPayload.demoFrom() == null || demoRequestStatusPayload.demoTo() == null){
                return new ResponseEntity<>(Utils.DEMO_FROM_TO_DATE_REQUIRED, HttpStatus.BAD_REQUEST);
            }

            if (!Utils.checkDateIsFromFutureOrPresent(demoRequestStatusPayload.demoFrom())) {
                return new ResponseEntity<>(Utils.DATE_IS_NOT_FROM_FUTURE_OR_PRESENT, HttpStatus.BAD_REQUEST);
            }

            DemoType demoType;
            try {
                demoType = DemoType.valueOf(demoRequestStatusPayload.demoType());
            } catch (IllegalArgumentException | NullPointerException e) {
                return new ResponseEntity<>(Utils.DEMO_TYPE_NOT_FOUND, HttpStatus.BAD_REQUEST);
            }

            demoRequest.setDemoDateFrom(Utils.localDateTimeToDate(demoRequestStatusPayload.demoFrom()));
            demoRequest.setDemoDateTo(Utils.localDateTimeToDate(demoRequestStatusPayload.demoTo()));
            demoRequest.setDemoType(demoType.name());
            demoRequest.setDemoMeetLink(demoRequestStatusPayload.demoMeetLink());
        }

        if (requestStatus.name().equals(DemoRequestStatus.DEMO_COMPLETED.name())) {

            if (demoRequestStatusPayload.presentedBy() == null || demoRequestStatusPayload.presentedBy().isBlank()){
                return new ResponseEntity<>(Utils.PRESENTED_BY_REQUIRED, HttpStatus.BAD_REQUEST);
            }
            if (demoRequestStatusPayload.presentedAt() == null){
                return new ResponseEntity<>(Utils.PRESENTED_AT_REQUIRED, HttpStatus.BAD_REQUEST);
            }

            Agent presentedBy = agentService.findUserByUserId(demoRequestStatusPayload.presentedBy());
            if (presentedBy == null){
                return new ResponseEntity<>(Utils.NO_AGENT_FOUND, HttpStatus.BAD_REQUEST);
            }

            demoRequest.setIsDemoCompleted(true);
            demoRequest.setPresentedBy(presentedBy.getAgentId());
            demoRequest.setPresentedAt(Utils.localDateTimeToDate(demoRequestStatusPayload.presentedAt()));
        }

        if (requestStatus.name().equals(DemoRequestStatus.TRIAL_STARTED.name())) {

            if (demoRequestStatusPayload.parentId() == null || demoRequestStatusPayload.parentId().isBlank()){
                return new ResponseEntity<>(Utils.PARENT_ID_REQUIRED, HttpStatus.BAD_REQUEST);
            }

            Users owner = usersService.getOwner(demoRequestStatusPayload.parentId());
            if (owner == null){
                return new ResponseEntity<>(Utils.NO_OWNER_FOUND, HttpStatus.BAD_REQUEST);
            }

            demoRequest.setParentId(demoRequestStatusPayload.parentId());
        }

        if (requestStatus.name().equals(DemoRequestStatus.DROPPED.name())) {
            DropReason dropReason;
            try {
                dropReason = DropReason.valueOf(demoRequestStatusPayload.dropReason());
            } catch (IllegalArgumentException | NullPointerException e) {
                return new ResponseEntity<>(Utils.DROP_REASON_NOT_FOUND, HttpStatus.BAD_REQUEST);
            }

            demoRequest.setDropReason(dropReason.name());
        }

        demoRequest = demoRequestRepository.save(demoRequest);

        DemoRequestActivity demoRequestActivity = new DemoRequestActivity();
        demoRequestActivity.setComment(demoRequestStatusPayload.comments());
        demoRequestActivity.setDescription(requestStatus.getDescription());
        demoRequestActivity.setStatus(requestStatus.name());
        demoRequestActivity.setCreatedByUserType(UserType.AGENT.name());
        demoRequestActivity.setCreatedBy(authentication.getName());
        demoRequestActivity.setCreatedAt(new Date());
        demoRequestActivity.setRequestId(demoRequest.getRequestId());

        demoRequestActivity = demoRequestActivityService.save(demoRequestActivity);

        DemoRequestSnapshot newRequest = SnapshotUtility.toSnapshot(demoRequest);

        agentActivitiesService.createAgentActivity(agent, ActivityType.UPDATE, Source.DEMO_REQUEST,
                String.valueOf(demoRequestId), oldRequest, newRequest);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ResponseEntity<?> deleteDemoRequest(Long demoRequestId) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        DemoRequest demoRequest = demoRequestRepository.findByRequestId(demoRequestId);
        if (demoRequest == null) {
            return new ResponseEntity<>(Utils.DEMO_REQUEST_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        DemoRequestSnapshot oldRequest = SnapshotUtility.toSnapshot(demoRequest);

        demoRequestRepository.delete(demoRequest);

        agentActivitiesService.createAgentActivity(agent, ActivityType.DELETE, Source.DEMO_REQUEST,
                String.valueOf(demoRequestId), oldRequest, null);

        return new ResponseEntity<>(Utils.DELETED, HttpStatus.OK);
    }

    public ResponseEntity<?> addDemoRequestComment(Long demoRequestId, DemoRequestCommentPayload payload) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        DemoRequest demoRequest = demoRequestRepository.findByRequestId(demoRequestId);
        if (demoRequest == null) {
            return new ResponseEntity<>(Utils.DEMO_REQUEST_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        DemoRequestComments demoRequestComments = new DemoRequestComments();

        demoRequestComments.setComment(payload.comment());
        demoRequestComments.setCreatedByUserType(UserType.AGENT.name());
        demoRequestComments.setCreatedBy(agent.getAgentId());
        demoRequestComments.setCreatedAt(new Date());
        demoRequestComments.setRequestId(demoRequest.getRequestId());

        demoRequestComments = demoRequestCommentsService.save(demoRequestComments);

        DemoRequestCommentsSnapshot demoRequestCommentsSnapshot = SnapshotUtility.toSnapshot(demoRequestComments);

        agentActivitiesService.createAgentActivity(agent, ActivityType.CREATE, Source.DEMO_REQUEST_COMMENTS,
                String.valueOf(demoRequestComments.getId()), null, demoRequestCommentsSnapshot);

        return new ResponseEntity<>(Utils.CREATED, HttpStatus.OK);
    }

    public ResponseEntity<?> getDemoRequestComment(Long demoRequestId) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        DemoRequest demoRequest = demoRequestRepository.findByRequestId(demoRequestId);
        if (demoRequest == null) {
            return new ResponseEntity<>(Utils.DEMO_REQUEST_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        List<DemoRequestComments> demoRequestComments = demoRequestCommentsService
                .getDemoRequestCommentsByRequestId(demoRequestId);

        Set<String> commentCreatedByIds = new HashSet<>();

        for (DemoRequestComments demoRequestComment : demoRequestComments) {
            if (UserType.AGENT.name().equals(demoRequestComment.getCreatedByUserType())){
                commentCreatedByIds.add(demoRequestComment.getCreatedBy());
            }
        }

        List<Agent> agents = agentService.getAgentsByIds(commentCreatedByIds);
        Map<String, Agent> agentMap = agents.stream()
                .collect(Collectors.toMap(Agent::getAgentId, a -> a));

        List<DemoRequestCommentsResponse> response = demoRequestComments.stream()
                .map(comment -> {

                    String createdBy = null;
                    Agent createdByAgent = agentMap.getOrDefault(comment.getCreatedBy(), null);
                    if (createdByAgent != null){
                        createdBy = Utils.getFullName(createdByAgent.getFirstName(), createdByAgent.getLastName());
                    }

                    return new DemoRequestCommentsResponse(comment.getId(),
                            comment.getComment(), comment.getCreatedByUserType(), createdBy,
                            Utils.dateToString(comment.getCreatedAt()), Utils.dateToTime(comment.getCreatedAt()));
                }).toList();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> getDemoRequestStatus() {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        List<DemoRequestStatusFlowResponse> response = Arrays.stream(DemoRequestStatus.values())
                .map(requestStatus -> new DemoRequestStatusFlowResponse(
                        requestStatus.name(),
                        requestStatus.getAllowedStatuses().stream()
                                .map(allowedStatus -> new DemoRequestStatusResponse(
                                        allowedStatus.name(),
                                        allowedStatus.getValue()
                                )).toList())
                ).toList();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> getDemoType() {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        List<DemoTypeResponse> response = Arrays.stream(DemoType.values())
                .map(demoType -> new DemoTypeResponse(demoType.name(),
                        demoType.getValue()))
                .toList();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> getDropReason() {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        List<DropReasonResponse> response = Arrays.stream(DropReason.values())
                .map(dropReason -> new DropReasonResponse(
                        dropReason.name(), dropReason.getValue()))
                .toList();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public long getDemoRequestCount(){
        return demoRequestRepository.getCount();
    }
}
