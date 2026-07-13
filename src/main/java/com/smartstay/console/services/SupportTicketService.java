package com.smartstay.console.services;

import com.smartstay.console.Mapper.supportTicket.SupportTicketListResMapper;
import com.smartstay.console.Mapper.supportTicket.SupportTicketResMapper;
import com.smartstay.console.config.Authentication;
import com.smartstay.console.config.FilesConfig;
import com.smartstay.console.config.UploadFileToS3;
import com.smartstay.console.dao.*;
import com.smartstay.console.dto.supportTicket.SupportTicketNotesSnapshot;
import com.smartstay.console.dto.supportTicket.SupportTicketSnapshot;
import com.smartstay.console.dto.supportTicket.SupportTicketStatsProjection;
import com.smartstay.console.ennum.*;
import com.smartstay.console.payloads.supportTicket.SupportTicketAssignPayload;
import com.smartstay.console.payloads.supportTicket.SupportTicketNotesPayload;
import com.smartstay.console.payloads.supportTicket.SupportTicketPayload;
import com.smartstay.console.payloads.supportTicket.SupportTicketStatusPayload;
import com.smartstay.console.repositories.SupportTicketRepository;
import com.smartstay.console.responses.supportTicket.*;
import com.smartstay.console.utils.SnapshotUtility;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SupportTicketService {

    @Autowired
    private SupportTicketRepository supportTicketRepository;
    @Autowired
    private Authentication authentication;
    @Autowired
    private AgentService agentService;
    @Autowired
    private AgentActivitiesService agentActivitiesService;
    @Autowired
    private UsersService usersService;
    @Autowired
    private HostelService hostelService;
    @Autowired
    private UploadFileToS3 uploadFileToS3;
    @Autowired
    private SupportTicketActivityService supportTicketActivityService;
    @Autowired
    private UserHostelService userHostelService;
    @Autowired
    private SupportTicketNotesService supportTicketNotesService;

    public ResponseEntity<?> addSupportTicket(SupportTicketPayload payload, MultipartFile paymentProof) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        String parentId = payload.parentId();
        Users owner = usersService.getOwner(parentId);
        if (owner == null) {
            return new ResponseEntity<>(Utils.NO_OWNER_FOUND, HttpStatus.BAD_REQUEST);
        }

        String hostelId = payload.hostelId();
        HostelV1 hostel = hostelService.getHostelByHostelId(hostelId);
        if (hostel == null) {
            return new ResponseEntity<>(Utils.NO_HOSTEL_FOUND, HttpStatus.BAD_REQUEST);
        }

        if (!hostel.getParentId().equals(parentId)){
            return new ResponseEntity<>(Utils.HOSTEL_PARENT_ID_MISMATCH, HttpStatus.BAD_REQUEST);
        }

        String raisedById = payload.raisedBy();
        Users raisedBy = usersService.getUserById(raisedById);
        if (raisedBy == null) {
            return new ResponseEntity<>(Utils.USER_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        boolean raisedByExists = userHostelService.existsByHostelIdAndUserId(hostelId, raisedById);
        if (!raisedByExists) {
            return new ResponseEntity<>(Utils.USER_HOSTEL_MISMATCH, HttpStatus.BAD_REQUEST);
        }

        QueryType queryType;
        try {
            queryType = QueryType.valueOf(payload.queryType());
        } catch (Exception e) {
            return new ResponseEntity<>(Utils.QUERY_TYPE_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        Date issueDate = Utils.localDateToDate(payload.issueDate());

        String paymentProofUrl = null;
        if (paymentProof != null) {
            try {
                paymentProofUrl = uploadFileToS3.uploadFileToS3(
                        FilesConfig.convertMultipartToFileNew(paymentProof), "support-ticket/payment-proof");
            } catch (Exception e) {
                return new ResponseEntity<>(Utils.FILE_UPLOAD_FAILED, HttpStatus.BAD_REQUEST);
            }
        }

        Date today = new Date();

        SupportTicket supportTicket = new SupportTicket();

        supportTicket.setParentId(parentId);
        supportTicket.setHostelId(hostelId);
        supportTicket.setRaisedBy(raisedById);
        supportTicket.setQueryType(queryType.name());
        supportTicket.setSubject(payload.subject());
        supportTicket.setIssueDate(issueDate);
        supportTicket.setRemarks(payload.remarks());
        supportTicket.setPaymentProof(paymentProofUrl);
        supportTicket.setSource(TicketSource.CONSOLE.name());
        supportTicket.setTicketStatus(TicketStatus.WAITING.name());
        supportTicket.setCreatedByUserType(UserType.AGENT.name());
        supportTicket.setCreatedBy(authentication.getName());
        supportTicket.setCreatedAt(today);

        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                supportTicket.setTicketNumber(generateTicketNumber());
                supportTicket = supportTicketRepository.save(supportTicket);
                break;
            } catch (Exception e) {
                if (attempt == 3) {
                    return new ResponseEntity<>(Utils.TICKET_NUMBER_GENERATION_FAILED, HttpStatus.BAD_REQUEST);
                }
            }
        }

        SupportTicketActivity activity = new SupportTicketActivity();
        activity.setComment(payload.remarks());
        activity.setDescription(TicketStatus.WAITING.getDescription());
        activity.setStatus(TicketStatus.WAITING.name());
        activity.setCreatedByUserType(UserType.AGENT.name());
        activity.setCreatedBy(authentication.getName());
        activity.setCreatedAt(today);
        activity.setTicketId(supportTicket.getTicketId());

        activity = supportTicketActivityService.save(activity);

        SupportTicketSnapshot snapshot = SnapshotUtility.toSnapshot(supportTicket);

        agentActivitiesService.createAgentActivity(agent, ActivityType.CREATE, Source.SUPPORT_TICKET,
                String.valueOf(supportTicket.getTicketId()), null, snapshot);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    public String generateTicketNumber() {

        int year = LocalDate.now().getYear();

        Optional<SupportTicket> latestTicket = supportTicketRepository
                .findLatestTicketForYear(year);

        long nextSequence = 1;

        if (latestTicket.isPresent()) {

            String lastTicketNo = latestTicket.get().getTicketNumber();

            // ST-2026-0001
            String[] parts = lastTicketNo.split("-");

            long lastSequence = Long.parseLong(parts[2]);

            nextSequence = lastSequence + 1;
        }

        int digits = Math.max(4, String.valueOf(nextSequence).length());

        return String.format(
                "ST-%d-%0" + digits + "d",
                year,
                nextSequence
        );
    }

    public ResponseEntity<?> getStatus() {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        List<TicketCurrentStatusResponse> responses = Arrays.stream(TicketStatus.values())
                .map(currentStatus -> {
                    List<TicketAllowedStatusResponse> allowedStatusResponses = currentStatus
                            .getAllowedStatuses().stream()
                            .filter(allowedStatus ->
                                    !(currentStatus == TicketStatus.ASSIGNED
                                            && allowedStatus == TicketStatus.ASSIGNED))
                            .map(allowedStatus -> new TicketAllowedStatusResponse(
                                    allowedStatus.name(), allowedStatus.getLabel()))
                            .toList();

                    return new TicketCurrentStatusResponse(currentStatus.name(),
                            currentStatus.getLabel(), allowedStatusResponses);
                }).toList();

        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    public ResponseEntity<?> getQueryType() {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        List<QueryTypeResponse> responses = Arrays.stream(QueryType.values())
                .map(queryType -> new QueryTypeResponse(queryType.name(),
                        queryType.getLabel()))
                .toList();

        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    public ResponseEntity<?> getPriority() {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        List<PriorityResponse> responses = Arrays.stream(Priority.values())
                .map(priority -> new PriorityResponse(priority.name(),
                        priority.getLabel()))
                .toList();

        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    public ResponseEntity<?> getAllSupportTickets(int page, int size, String name,
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

        if (startDate == null) {
            startDate = currentMonthStartDate;
        }
        if (endDate == null) {
            endDate = currentMonthEndDate;
        }
        endDate = Utils.addDaysToDate(endDate, 1);

        SupportTicketStatsProjection stats = supportTicketRepository
                .getDashboardStats(now, nowEnds, startDate, endDate);

        long totalLeads = stats.getTotalLeads();
        long todayNewCount = stats.getTodayNewCount();
        long waitingCount = stats.getWaitingCount();
        long assignedCount = stats.getAssignedCount();
        long inProgressCount = stats.getInProgressCount();
        long resolvedCount = stats.getResolvedCount();
        long closedCount = stats.getClosedCount();

        name = (name == null || name.isBlank()) ? null : name.trim();
        status = (status == null || status.isBlank()) ? null : status.trim();
        agentId = (agentId == null || agentId.isBlank()) ? null : agentId.trim();

        Set<Long> ticketIds = null;
        if (name != null){
            Set<Long> ids = new HashSet<>();

            // 1. Search by ticket number
            List<SupportTicket> tickets = supportTicketRepository
                    .findByTicketNumberContainingIgnoreCase(name);

            tickets.forEach(ticket ->
                    ids.add(ticket.getTicketId())
            );

            // 2. Search hostel name from another table
            Set<String> hostelIds = hostelService
                    .getHostelsByHostelName(name)
                    .stream()
                    .map(HostelV1::getHostelId)
                    .collect(Collectors.toSet());

            if (!hostelIds.isEmpty()) {
                List<SupportTicket> hostelTickets = supportTicketRepository
                        .findAllByHostelIdIn(hostelIds);

                hostelTickets.forEach(ticket ->
                        ids.add(ticket.getTicketId())
                );
            }

            ticketIds = ids;

            // no match found
            if(ticketIds.isEmpty()){
                Map<String, Object> response = new HashMap<>();
                response.put("supportTicketList", Collections.emptyList());
                response.put("currentPage", page + 1);
                response.put("pageSize", size);
                response.put("totalItems", 0);
                response.put("totalPages", 0);
                response.put("totalLeads", totalLeads);
                response.put("newToday", todayNewCount);
                response.put("waitingCount", waitingCount);
                response.put("assignedCount", assignedCount);
                response.put("inProgressCount", inProgressCount);
                response.put("resolvedCount", resolvedCount);
                response.put("closedCount", closedCount);

                return new ResponseEntity<>(response, HttpStatus.OK);
            }
        }

        page = Math.max(page - 1, 0);
        size = Math.max(size, 1);

        Pageable pageable = PageRequest.of(page, size);

        Page<SupportTicket> pagedSupportTickets = supportTicketRepository
                .findAllPaginated(ticketIds, startDate, endDate, status, agentId, pageable);

        List<SupportTicket> supportTickets = pagedSupportTickets.getContent();

        Set<String> parentIds = new HashSet<>();
        Set<String> hostelIds = new HashSet<>();
        Set<String> raisedByIds = new HashSet<>();
        Set<String> assignedToIds = new HashSet<>();
        Set<String> assignedByIds = new HashSet<>();
        Set<String> createdByAgentIds = new HashSet<>();
        Set<String> createdByUserIds = new HashSet<>();
        for (SupportTicket supportTicket : supportTickets) {
            parentIds.add(supportTicket.getParentId());
            hostelIds.add(supportTicket.getHostelId());
            raisedByIds.add(supportTicket.getRaisedBy());
            assignedToIds.add(supportTicket.getAssignedTo());
            assignedByIds.add(supportTicket.getAssignedBy());
            if (UserType.AGENT.name().equals(supportTicket.getCreatedByUserType())){
                createdByAgentIds.add(supportTicket.getCreatedBy());
            } else {
                createdByUserIds.add(supportTicket.getCreatedBy());
            }
        }

        Set<String> userIds = new HashSet<>();
        userIds.addAll(raisedByIds);
        userIds.addAll(createdByUserIds);

        Set<String> agentIds = new HashSet<>();
        agentIds.addAll(assignedToIds);
        agentIds.addAll(assignedByIds);
        agentIds.addAll(createdByAgentIds);

        List<Users> owners = usersService.getOwners(new ArrayList<>(parentIds));
        Map<String, Users> ownerByParentIdMap = owners.stream()
                .collect(Collectors.toMap(Users::getParentId, user -> user));

        List<HostelV1> hostels = hostelService.getHostelsByHostelIds(hostelIds);
        Map<String, HostelV1> hostelByHostelIdMap = hostels.stream()
                .collect(Collectors.toMap(HostelV1::getHostelId, hostel -> hostel));

        List<Users> users = usersService.getUsersByIds(userIds);
        Map<String, Users> userByUserIdMap = users.stream()
                .collect(Collectors.toMap(Users::getUserId, user -> user));

        List<Agent> agents = agentService.getAgentsByIds(agentIds);
        Map<String, Agent> agentByAgentIdMap = agents.stream()
                .collect(Collectors.toMap(Agent::getAgentId, a -> a));

        List<SupportTicketListResponse> responses = supportTickets.stream()
                .map(supportTicket -> {
                    Users owner = ownerByParentIdMap.getOrDefault(supportTicket.getParentId(), null);
                    HostelV1 hostel = hostelByHostelIdMap.getOrDefault(supportTicket.getHostelId(), null);
                    return new SupportTicketListResMapper(owner, hostel,
                            userByUserIdMap, agentByAgentIdMap)
                            .apply(supportTicket);
                }).toList();

        Map<String, Object> response = new HashMap<>();
        response.put("supportTicketList", responses);
        response.put("currentPage", page + 1);
        response.put("pageSize", size);
        response.put("totalItems", pagedSupportTickets.getTotalElements());
        response.put("totalPages", pagedSupportTickets.getTotalPages());
        response.put("totalLeads", totalLeads);
        response.put("newToday", todayNewCount);
        response.put("waitingCount", waitingCount);
        response.put("assignedCount", assignedCount);
        response.put("inProgressCount", inProgressCount);
        response.put("resolvedCount", resolvedCount);
        response.put("closedCount", closedCount);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> getSupportTicketById(Long supportTicketId) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        SupportTicket supportTicket = supportTicketRepository.findByTicketId(supportTicketId);
        if (supportTicket == null){
            return new ResponseEntity<>(Utils.SUPPORT_TICKET_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        Set<String> userIds = new HashSet<>();
        userIds.add(supportTicket.getRaisedBy());

        Set<String> agentIds = new HashSet<>();
        agentIds.add(supportTicket.getAssignedTo());
        agentIds.add(supportTicket.getAssignedBy());

        if (UserType.AGENT.name().equals(supportTicket.getCreatedByUserType())){
            agentIds.add(supportTicket.getCreatedBy());
        } else {
            userIds.add(supportTicket.getCreatedBy());
        }

        List<SupportTicketActivity> supportTicketActivities = supportTicketActivityService
                .getAllByTicketId(supportTicketId);

        Set<String> createdByAgentIds = new HashSet<>();
        Set<String> createdByUserIds = new HashSet<>();
        for (SupportTicketActivity supportTicketActivity : supportTicketActivities) {
            if (UserType.AGENT.name().equals(supportTicketActivity.getCreatedByUserType())){
                createdByAgentIds.add(supportTicketActivity.getCreatedBy());
            } else {
                createdByUserIds.add(supportTicketActivity.getCreatedBy());
            }
        }

        userIds.addAll(createdByUserIds);
        agentIds.addAll(createdByAgentIds);

        Users owner = usersService.getOwner(supportTicket.getParentId());

        HostelV1 hostel = hostelService.getHostelByHostelId(supportTicket.getHostelId());

        List<Users> users = usersService.getUsersByIds(userIds);
        Map<String, Users> userByUserIdMap = users.stream()
                .collect(Collectors.toMap(Users::getUserId, user -> user));

        List<Agent> agents = agentService.getAgentsByIds(agentIds);
        Map<String, Agent> agentByAgentIdMap = agents.stream()
                .collect(Collectors.toMap(Agent::getAgentId, a -> a));

        SupportTicketResponse response = new SupportTicketResMapper(
                owner, hostel, userByUserIdMap, agentByAgentIdMap, supportTicketActivities
        ).apply(supportTicket);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> assignSupportTicket(Long supportTicketId, SupportTicketAssignPayload payload) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        SupportTicket supportTicket = supportTicketRepository.findByTicketId(supportTicketId);
        if (supportTicket == null){
            return new ResponseEntity<>(Utils.SUPPORT_TICKET_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        SupportTicketSnapshot oldTicket = SnapshotUtility.toSnapshot(supportTicket);

        TicketStatus currentStatus;
        try {
            currentStatus = TicketStatus.valueOf(supportTicket.getTicketStatus());
        } catch (IllegalArgumentException | NullPointerException e) {
            return new ResponseEntity<>(Utils.SUPPORT_TICKET_STATUS_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        if (!currentStatus.canMoveTo(TicketStatus.ASSIGNED)) {
            return new ResponseEntity<>(Utils.INVALID_STATUS_TRANSITION, HttpStatus.BAD_REQUEST);
        }

        Agent assignedTo = agentService.findUserByUserId(payload.agentId());
        if (assignedTo == null) {
            return new ResponseEntity<>(Utils.NO_AGENT_FOUND, HttpStatus.BAD_REQUEST);
        }

        Priority priority;
        try {
            priority = Priority.valueOf(payload.priority());
        } catch (IllegalArgumentException | NullPointerException e) {
            return new ResponseEntity<>(Utils.PRIORITY_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        if (supportTicket.getAssignedTo() != null){
            if (payload.comments() == null || payload.comments().isBlank()){
                return new ResponseEntity<>(Utils.COMMENTS_REQUIRED_FOR_REASSIGN_STAFF, HttpStatus.BAD_REQUEST);
            }
        }

        Date today = new Date();

        supportTicket.setPriority(priority.name());
        supportTicket.setAssignedTo(assignedTo.getAgentId());
        supportTicket.setAssignedBy(authentication.getName());
        supportTicket.setTicketStatus(TicketStatus.ASSIGNED.name());

        supportTicket = supportTicketRepository.save(supportTicket);

        SupportTicketActivity activity = new SupportTicketActivity();
        activity.setComment(payload.comments());
        activity.setDescription(TicketStatus.ASSIGNED.getDescription());
        activity.setStatus(TicketStatus.ASSIGNED.name());
        activity.setCreatedByUserType(UserType.AGENT.name());
        activity.setCreatedBy(authentication.getName());
        activity.setCreatedAt(today);
        activity.setTicketId(supportTicket.getTicketId());

        activity = supportTicketActivityService.save(activity);

        SupportTicketSnapshot newTicket = SnapshotUtility.toSnapshot(supportTicket);

        agentActivitiesService.createAgentActivity(agent, ActivityType.UPDATE, Source.SUPPORT_TICKET,
                String.valueOf(supportTicketId), oldTicket, newTicket);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ResponseEntity<?> updateSupportTicketStatus(Long supportTicketId, SupportTicketStatusPayload payload) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        SupportTicket supportTicket = supportTicketRepository.findByTicketId(supportTicketId);
        if (supportTicket == null){
            return new ResponseEntity<>(Utils.SUPPORT_TICKET_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        SupportTicketSnapshot oldTicket = SnapshotUtility.toSnapshot(supportTicket);

        TicketStatus requestStatus;
        try {
            requestStatus = TicketStatus.valueOf(payload.ticketStatus());
        } catch (IllegalArgumentException | NullPointerException e){
            return new ResponseEntity<>(Utils.SUPPORT_TICKET_STATUS_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        TicketStatus currentStatus;
        try {
            currentStatus = TicketStatus.valueOf(supportTicket.getTicketStatus());
        } catch (IllegalArgumentException | NullPointerException e){
            return new ResponseEntity<>(Utils.SUPPORT_TICKET_STATUS_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        if (!currentStatus.canMoveTo(requestStatus)) {
            return new ResponseEntity<>(Utils.INVALID_STATUS_TRANSITION, HttpStatus.BAD_REQUEST);
        }

        Date today = new Date();

        supportTicket.setTicketStatus(requestStatus.name());

        if (requestStatus.name().equals(TicketStatus.ASSIGNED.name())) {

            if (payload.agentId() == null || payload.agentId().isBlank()){
                return new ResponseEntity<>(Utils.AGENT_ID_REQUIRED, HttpStatus.BAD_REQUEST);
            }

            if (payload.priority() == null || payload.priority().isBlank()){
                return new ResponseEntity<>(Utils.PRIORITY_REQUIRED, HttpStatus.BAD_REQUEST);
            }

            if (supportTicket.getAssignedTo() != null){
                if (payload.comments() == null || payload.comments().isBlank()){
                    return new ResponseEntity<>(Utils.COMMENTS_REQUIRED_FOR_REASSIGN_STAFF, HttpStatus.BAD_REQUEST);
                }
            }

            Agent assignedTo = agentService.findUserByUserId(payload.agentId());
            if (assignedTo == null) {
                return new ResponseEntity<>(Utils.NO_AGENT_FOUND, HttpStatus.BAD_REQUEST);
            }

            Priority priority;
            try {
                priority = Priority.valueOf(payload.priority());
            } catch (IllegalArgumentException | NullPointerException e) {
                return new ResponseEntity<>(Utils.PRIORITY_NOT_FOUND, HttpStatus.BAD_REQUEST);
            }

            supportTicket.setPriority(priority.name());
            supportTicket.setAssignedTo(assignedTo.getAgentId());
            supportTicket.setAssignedBy(authentication.getName());
        }

        supportTicket = supportTicketRepository.save(supportTicket);

        SupportTicketActivity activity = new SupportTicketActivity();
        activity.setComment(payload.comments());
        activity.setDescription(requestStatus.getDescription());
        activity.setStatus(requestStatus.name());
        activity.setCreatedByUserType(UserType.AGENT.name());
        activity.setCreatedBy(authentication.getName());
        activity.setCreatedAt(today);
        activity.setTicketId(supportTicket.getTicketId());

        activity = supportTicketActivityService.save(activity);

        SupportTicketSnapshot newTicket = SnapshotUtility.toSnapshot(supportTicket);

        agentActivitiesService.createAgentActivity(agent, ActivityType.UPDATE, Source.SUPPORT_TICKET,
                String.valueOf(supportTicketId), oldTicket, newTicket);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ResponseEntity<?> addSupportTicketNotes(Long supportTicketId, SupportTicketNotesPayload payload) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        SupportTicket supportTicket = supportTicketRepository.findByTicketId(supportTicketId);
        if (supportTicket == null){
            return new ResponseEntity<>(Utils.SUPPORT_TICKET_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        Date today = new Date();

        SupportTicketNotes supportTicketNotes = new SupportTicketNotes();

        supportTicketNotes.setComment(payload.notes());
        supportTicketNotes.setCreatedByUserType(UserType.AGENT.name());
        supportTicketNotes.setCreatedBy(authentication.getName());
        supportTicketNotes.setCreatedAt(today);
        supportTicketNotes.setTicketId(supportTicketId);

        supportTicketNotes = supportTicketNotesService.save(supportTicketNotes);

        SupportTicketNotesSnapshot newNotes = SnapshotUtility.toSnapshot(supportTicketNotes);

        agentActivitiesService.createAgentActivity(agent, ActivityType.CREATE, Source.SUPPORT_TICKET_NOTES,
                String.valueOf(supportTicketNotes.getId()), null, newNotes);

        return new ResponseEntity<>(Utils.CREATED, HttpStatus.OK);
    }

    public ResponseEntity<?> getSupportTicketNotes(Long supportTicketId) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        SupportTicket supportTicket = supportTicketRepository.findByTicketId(supportTicketId);
        if (supportTicket == null){
            return new ResponseEntity<>(Utils.SUPPORT_TICKET_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        List<SupportTicketNotes> supportTicketNotes = supportTicketNotesService
                .getSupportTicketNotesByTicketId(supportTicketId);

        Set<String> notesCreatedByAgentIds = new HashSet<>();
        for (SupportTicketNotes note : supportTicketNotes) {
            if (UserType.AGENT.name().equals(note.getCreatedByUserType())){
                notesCreatedByAgentIds.add(note.getCreatedBy());
            }
        }

        List<Agent> agents = agentService.getAgentsByIds(notesCreatedByAgentIds);
        Map<String, Agent> agentMap = agents.stream()
                .collect(Collectors.toMap(Agent::getAgentId, a -> a));

        List<SupportTicketNotesResponse> response = supportTicketNotes.stream()
                .map(notes -> {

                    String createdBy = null;
                    Agent createdByAgent = agentMap.getOrDefault(notes.getCreatedBy(), null);
                    if (createdByAgent != null){
                        createdBy = Utils.getFullName(createdByAgent.getFirstName(), createdByAgent.getLastName());
                    }

                    return new SupportTicketNotesResponse(notes.getId(), notes.getComment(),
                            notes.getCreatedByUserType(), notes.getCreatedBy(), createdBy,
                            Utils.dateToString(notes.getCreatedAt()), Utils.dateToTime(notes.getCreatedAt()));
                }).toList();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
