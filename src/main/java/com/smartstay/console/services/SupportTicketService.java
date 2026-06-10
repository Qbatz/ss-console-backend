package com.smartstay.console.services;

import com.smartstay.console.config.Authentication;
import com.smartstay.console.config.FilesConfig;
import com.smartstay.console.config.UploadFileToS3;
import com.smartstay.console.dao.*;
import com.smartstay.console.dto.supportTicket.SupportTicketSnapshot;
import com.smartstay.console.ennum.*;
import com.smartstay.console.payloads.supportTicket.SupportTicketPayload;
import com.smartstay.console.repositories.SupportTicketRepository;
import com.smartstay.console.responses.supportTicket.PriorityResponse;
import com.smartstay.console.responses.supportTicket.QueryTypeResponse;
import com.smartstay.console.responses.supportTicket.TicketAllowedStatusResponse;
import com.smartstay.console.responses.supportTicket.TicketCurrentStatusResponse;
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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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

        page = Math.max(page - 1, 0);
        size = Math.max(size, 1);

        Pageable pageable = PageRequest.of(page, size);

        Page<SupportTicket> pagedSupportTickets = supportTicketRepository
                .findAll(pageable);

        List<SupportTicket> supportTickets = pagedSupportTickets.getContent();

        return new ResponseEntity<>(supportTickets, HttpStatus.OK);
    }
}
