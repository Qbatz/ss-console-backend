package com.smartstay.console.services;

import com.smartstay.console.Mapper.demoRequests.DemoRequestMapper;
import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.*;
import com.smartstay.console.dto.demoRequest.DemoRequestCommentsSnapshot;
import com.smartstay.console.dto.demoRequest.DemoRequestSnapshot;
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
    private PlansService plansService;

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
        demoRequest.setDemoRequestStatus(RequestStatus.NEW.name());
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
        demoRequestActivity.setDescription(RequestStatus.NEW.getDescription());
        demoRequestActivity.setStatus(RequestStatus.NEW.name());
        demoRequestActivity.setCreatedByUserType(UserType.AGENT.name());
        demoRequestActivity.setCreatedBy(authentication.getName());
        demoRequestActivity.setCreatedAt(today);
        demoRequestActivity.setDemoRequest(demoRequest);

        demoRequestActivity = demoRequestActivityService.save(demoRequestActivity);

        if (demoRequest.getDemoRequestActivities() == null) {
            demoRequest.setDemoRequestActivities(new ArrayList<>());
        }
        demoRequest.getDemoRequestActivities().add(demoRequestActivity);

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

        Date currentMonthStartDate = Utils.getStartDateOfMonth(today);
        Date currentMonthEndDate = Utils.getEndDateOfMonth(today);
        Date currentMonthEndDatePlus1 = Utils.addDaysToDate(currentMonthEndDate, 1);

        long totalLeads = demoRequestRepository.getTotalLeadsCount(currentMonthStartDate, currentMonthEndDatePlus1);
        long todayNewCount = demoRequestRepository.getNewByDateCount(now);
        long contactedCount = demoRequestActivityService.getContactedCount(currentMonthStartDate, currentMonthEndDatePlus1);
        long demoScheduledCount = demoRequestActivityService.getDemoScheduledCount(currentMonthStartDate, currentMonthEndDatePlus1);

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

        Set<String> assignedToIds = new HashSet<>();
        Set<String> assignedByIds = new HashSet<>();
        Set<String> presentedByIds = new HashSet<>();
        Set<String> commentCreatedByIds = new HashSet<>();
        Set<String> activityCreatedByIds = new HashSet<>();
        Set<String> planCodes = new HashSet<>();

        for (DemoRequest demoRequest : demoRequests) {
            assignedToIds.add(demoRequest.getAssignedTo());
            assignedByIds.add(demoRequest.getAssignedBy());
            presentedByIds.add(demoRequest.getPresentedBy());

            planCodes.add(demoRequest.getConvertedToPlanCode());

            if (demoRequest.getDemoRequestComments() != null){
                for (DemoRequestComments demoRequestComment : demoRequest.getDemoRequestComments()) {
                    if (UserType.AGENT.name().equals(demoRequestComment.getCreatedByUserType())){
                        commentCreatedByIds.add(demoRequestComment.getCreatedBy());
                    }
                }
            }
            if (demoRequest.getDemoRequestActivities() != null){
                for (DemoRequestActivity demoRequestActivity : demoRequest.getDemoRequestActivities()) {
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

        List<Plans> plans = plansService.findPlansByPlanCodes(planCodes);
        Map<String, Plans> plansMap = plans.stream()
                .collect(Collectors.toMap(Plans::getPlanCode, a -> a));

        List<DemoRequestResponse> demoRequestList = demoRequests.stream()
                .map(demoRequest -> {
                    Plans plan = plansMap.getOrDefault(demoRequest.getConvertedToPlanCode(), null);
                    return new DemoRequestMapper(agentMap, plan).apply(demoRequest);
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
        response.put("contacted", contactedCount);
        response.put("demoScheduled", demoScheduledCount);

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

        if (demoRequest.getDemoRequestComments() != null){
            for (DemoRequestComments demoRequestComment : demoRequest.getDemoRequestComments()) {
                if (UserType.AGENT.name().equals(demoRequestComment.getCreatedByUserType())){
                    commentCreatedByIds.add(demoRequestComment.getCreatedBy());
                }
            }
        }

        if (demoRequest.getDemoRequestActivities() != null){
            for (DemoRequestActivity demoRequestActivity : demoRequest.getDemoRequestActivities()) {
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

        Plans plan = plansService.findPlanByPlanCode(demoRequest.getConvertedToPlanCode());

        DemoRequestResponse response = new DemoRequestMapper(agentMap, plan).apply(demoRequest);

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

        RequestStatus currentStatus;
        try {
            currentStatus = RequestStatus.valueOf(
                    demoRequest.getDemoRequestStatus()
            );
        } catch (IllegalArgumentException | NullPointerException e) {
            return new ResponseEntity<>(Utils.DEMO_REQUEST_STATUS_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        if (!currentStatus.canMoveTo(RequestStatus.ASSIGNED)) {
            return new ResponseEntity<>(Utils.INVALID_STATUS_TRANSITION, HttpStatus.BAD_REQUEST);
        }

        DemoRequestSnapshot oldRequest = SnapshotUtility.toSnapshot(demoRequest);

        Agent assignedTo = agentService.findUserByUserId(payload.agentId());
        if (assignedTo == null) {
            return new ResponseEntity<>(Utils.NO_AGENT_FOUND, HttpStatus.BAD_REQUEST);
        }

        demoRequest.setDemoRequestStatus(RequestStatus.ASSIGNED.name());
        demoRequest.setIsAssigned(true);
        demoRequest.setAssignedTo(assignedTo.getAgentId());
        demoRequest.setAssignedBy(agent.getAgentId());
        demoRequest.setComments(payload.comments());

        demoRequest = demoRequestRepository.save(demoRequest);

        DemoRequestActivity demoRequestActivity = new DemoRequestActivity();
        demoRequestActivity.setComment(payload.comments());
        demoRequestActivity.setDescription(RequestStatus.ASSIGNED.getDescription());
        demoRequestActivity.setStatus(RequestStatus.ASSIGNED.name());
        demoRequestActivity.setCreatedByUserType(UserType.AGENT.name());
        demoRequestActivity.setCreatedBy(authentication.getName());
        demoRequestActivity.setCreatedAt(new Date());
        demoRequestActivity.setDemoRequest(demoRequest);

        demoRequestActivity = demoRequestActivityService.save(demoRequestActivity);

        if (demoRequest.getDemoRequestActivities() == null) {
            demoRequest.setDemoRequestActivities(new ArrayList<>());
        }
        demoRequest.getDemoRequestActivities().add(demoRequestActivity);

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

        RequestStatus currentStatus;
        try {
            currentStatus = RequestStatus.valueOf(
                    demoRequest.getDemoRequestStatus()
            );
        } catch (IllegalArgumentException | NullPointerException e) {
            return new ResponseEntity<>(Utils.DEMO_REQUEST_STATUS_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        if (!currentStatus.canMoveTo(RequestStatus.DROPPED)) {
            return new ResponseEntity<>(Utils.INVALID_STATUS_TRANSITION, HttpStatus.BAD_REQUEST);
        }

        DemoRequestSnapshot oldRequest = SnapshotUtility.toSnapshot(demoRequest);

        DropReason dropReason;
        try {
            dropReason = DropReason.valueOf(payload.dropReason());
        } catch (IllegalArgumentException | NullPointerException e) {
            return new ResponseEntity<>(Utils.DROP_REASON_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        demoRequest.setDemoRequestStatus(RequestStatus.DROPPED.name());
        demoRequest.setDropReason(dropReason.name());
        demoRequest.setComments(payload.comments());

        demoRequest = demoRequestRepository.save(demoRequest);

        DemoRequestActivity demoRequestActivity = new DemoRequestActivity();
        demoRequestActivity.setComment(payload.comments());
        demoRequestActivity.setDescription(RequestStatus.DROPPED.getDescription());
        demoRequestActivity.setStatus(RequestStatus.DROPPED.name());
        demoRequestActivity.setCreatedByUserType(UserType.AGENT.name());
        demoRequestActivity.setCreatedBy(authentication.getName());
        demoRequestActivity.setCreatedAt(new Date());
        demoRequestActivity.setDemoRequest(demoRequest);

        demoRequestActivity = demoRequestActivityService.save(demoRequestActivity);

        if (demoRequest.getDemoRequestActivities() == null) {
            demoRequest.setDemoRequestActivities(new ArrayList<>());
        }
        demoRequest.getDemoRequestActivities().add(demoRequestActivity);

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

        RequestStatus requestStatus;
        try {
            requestStatus = RequestStatus.valueOf(
                    demoRequestStatusPayload.demoRequestStatus().toUpperCase()
            );
        } catch (IllegalArgumentException | NullPointerException e) {
            return new ResponseEntity<>(Utils.DEMO_REQUEST_STATUS_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        RequestStatus currentStatus;
        try {
            currentStatus = RequestStatus.valueOf(
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

        if (requestStatus.name().equals(RequestStatus.ASSIGNED.name())){

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

        if (requestStatus.name().equals(RequestStatus.DEMO_SCHEDULED.name())){

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

        if (requestStatus.name().equals(RequestStatus.DEMO_COMPLETED.name())) {

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

        if (requestStatus.name().equals(RequestStatus.TRIAL_STARTED.name())) {
            Plans trialPlan = plansService.findTrialPlan();
            String trialPlanCode = null;
            if (trialPlan != null){
                trialPlanCode = trialPlan.getPlanCode();
            }

            demoRequest.setConvertedToPlanCode(trialPlanCode);
        }

        if (requestStatus.name().equals(RequestStatus.CONVERTED.name())) {
            if (demoRequestStatusPayload.planCode() == null || demoRequestStatusPayload.planCode().isBlank()){
                return new ResponseEntity<>(Utils.PLAN_CODE_REQUIRED, HttpStatus.BAD_REQUEST);
            }

            Plans plan = plansService.findPlanByPlanCode(demoRequestStatusPayload.planCode());
            if (plan == null){
                return new ResponseEntity<>(Utils.PLAN_NOT_FOUND, HttpStatus.BAD_REQUEST);
            }

            demoRequest.setConvertedToPlanCode(demoRequestStatusPayload.planCode());
        }

        if (requestStatus.name().equals(RequestStatus.DROPPED.name())) {
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
        demoRequestActivity.setDemoRequest(demoRequest);

        demoRequestActivity = demoRequestActivityService.save(demoRequestActivity);

        if (demoRequest.getDemoRequestActivities() == null) {
            demoRequest.setDemoRequestActivities(new ArrayList<>());
        }
        demoRequest.getDemoRequestActivities().add(demoRequestActivity);

        DemoRequestSnapshot newRequest = SnapshotUtility.toSnapshot(demoRequest);

        agentActivitiesService.createAgentActivity(agent, ActivityType.UPDATE, Source.DEMO_REQUEST,
                String.valueOf(demoRequestId), oldRequest, newRequest);

        return new ResponseEntity<>(HttpStatus.OK);
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
        demoRequestComments.setDemoRequest(demoRequest);

        demoRequestComments = demoRequestCommentsService.save(demoRequestComments);

        if (demoRequest.getDemoRequestComments() == null) {
            demoRequest.setDemoRequestComments(new ArrayList<>());
        }
        demoRequest.getDemoRequestComments().add(demoRequestComments);

        DemoRequestCommentsSnapshot demoRequestCommentsSnapshot = SnapshotUtility.toSnapshot(demoRequestComments);

        agentActivitiesService.createAgentActivity(agent, ActivityType.CREATE, Source.DEMO_REQUEST_COMMENTS,
                String.valueOf(demoRequestComments.getId()), null, demoRequestCommentsSnapshot);

        return new ResponseEntity<>(Utils.CREATED, HttpStatus.OK);
    }

    public ResponseEntity<?> getDemoRequestStatus() {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        List<DemoRequestStatusFlowResponse> response = Arrays.stream(RequestStatus.values())
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
