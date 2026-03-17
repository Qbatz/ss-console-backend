package com.smartstay.console.services;

import com.smartstay.console.Mapper.demoRequests.DemoRequestMapper;
import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.Agent;
import com.smartstay.console.dao.DemoRequest;
import com.smartstay.console.ennum.ActivityType;
import com.smartstay.console.ennum.RequestStatus;
import com.smartstay.console.ennum.Source;
import com.smartstay.console.payloads.agent.AgentIdPayload;
import com.smartstay.console.payloads.demoRequest.DemoRequestPayload;
import com.smartstay.console.payloads.demoRequest.DemoRequestStatusPayload;
import com.smartstay.console.repositories.DemoRequestRepository;
import com.smartstay.console.responses.demoRequest.DemoRequestResponse;
import com.smartstay.console.responses.demoRequest.DemoRequestStatusResponse;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

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

    public ResponseEntity<?> getAllDemoRequests(int page, int size, String name){

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        page = Math.max(page - 1, 0);
        size = Math.max(size, 1);
        name = name == null || name.isBlank() ? null : name.trim();
        Pageable pageable = PageRequest.of(page, size);

        Page<DemoRequest> paginatedDemoRequest = demoRequestRepository
                .findAllPaginated(name, pageable);

        List<DemoRequest> demoRequests = paginatedDemoRequest.getContent();

        Set<String> assignedToIds = new HashSet<>();
        Set<String> assignedByIds = new HashSet<>();
        Set<String> presentedByIds = new HashSet<>();

        for (DemoRequest demoRequest : demoRequests) {
            assignedToIds.add(demoRequest.getAssignedTo());
            assignedByIds.add(demoRequest.getAssignedBy());
            presentedByIds.add(demoRequest.getPresentedBy());
        }

        Set<String> agentIds = new HashSet<>();
        agentIds.addAll(assignedToIds);
        agentIds.addAll(assignedByIds);
        agentIds.addAll(presentedByIds);

        List<Agent> agents = agentService.getAgentsByIds(agentIds);
        Map<String, Agent> agentMap = agents.stream()
                .collect(Collectors.toMap(Agent::getAgentId, a -> a));

        List<DemoRequestResponse> demoRequestList = demoRequests.stream()
                .map(demoRequest -> new DemoRequestMapper(agentMap)
                        .apply(demoRequest))
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("demoRequestList", demoRequestList);
        response.put("currentPage", page + 1);
        response.put("pageSize", size);
        response.put("totalItems", paginatedDemoRequest.getTotalElements());
        response.put("totalPages", paginatedDemoRequest.getTotalPages());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> assignDemoRequest(Long demoRequestId, AgentIdPayload agentIdPayload) {

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
        DemoRequest oldRequest = new ObjectMapper().convertValue(demoRequest, DemoRequest.class);

        Agent assignedTo = agentService.findUserByUserId(agentIdPayload.agentId());
        if (assignedTo == null) {
            return new ResponseEntity<>(Utils.NO_AGENT_FOUND, HttpStatus.BAD_REQUEST);
        }

        demoRequest.setDemoRequestStatus(RequestStatus.ASSIGNED.name());
        demoRequest.setIsAssigned(true);
        demoRequest.setAssignedTo(assignedTo.getAgentId());
        demoRequest.setAssignedBy(agent.getAgentId());

        demoRequest = demoRequestRepository.save(demoRequest);

        agentActivitiesService.createAgentActivity(agent, ActivityType.UPDATE, Source.DEMO_REQUEST,
                String.valueOf(oldRequest.getRequestId()), oldRequest, demoRequest);

        return new ResponseEntity<>(HttpStatus.OK);
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

        Set<String> agentIds = new HashSet<>();
        agentIds.add(demoRequest.getAssignedTo());
        agentIds.add(demoRequest.getAssignedBy());
        agentIds.add(demoRequest.getPresentedBy());

        List<Agent> agents = agentService.getAgentsByIds(agentIds);
        Map<String, Agent> agentMap = agents.stream()
                .collect(Collectors.toMap(Agent::getAgentId, a -> a));

        DemoRequestResponse response = new DemoRequestMapper(agentMap).apply(demoRequest);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> addDemoRequest(DemoRequestPayload demoRequestPayload) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

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
        demoRequest.setDemoRequestStatus(RequestStatus.REQUESTED.name());
        demoRequest.setIsDemoCompleted(false);
        demoRequest.setIsAssigned(false);
        demoRequest.setComments(demoRequestPayload.comments());
        demoRequest.setRequestedDate(Utils.localDateToString(demoRequestPayload.requestedDate()));
        demoRequest.setRequestedTime(Utils.localTimeToString(demoRequestPayload.requestedTime()));

        demoRequest = demoRequestRepository.save(demoRequest);

        agentActivitiesService.createAgentActivity(agent, ActivityType.CREATE, Source.DEMO_REQUEST,
                String.valueOf(demoRequest.getRequestId()), null, demoRequest);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    public long getDemoRequestCount(){
        return demoRequestRepository.getCount();
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
        DemoRequest oldRequest = new ObjectMapper().convertValue(demoRequest, DemoRequest.class);

        RequestStatus requestStatus;

        try {
            requestStatus = RequestStatus.valueOf(
                    demoRequestStatusPayload.demoRequestStatus().toUpperCase()
            );
        } catch (IllegalArgumentException | NullPointerException e) {
            return new ResponseEntity<>(Utils.DEMO_REQUEST_STATUS_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        demoRequest.setDemoRequestStatus(requestStatus.name());

        if (demoRequestStatusPayload.comments() != null && !demoRequestStatusPayload.comments().isBlank()){
            demoRequest.setComments(demoRequestStatusPayload.comments());
        }

        if (requestStatus.name().equals(RequestStatus.COMPLETED.name())) {

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

        demoRequest = demoRequestRepository.save(demoRequest);

        agentActivitiesService.createAgentActivity(agent, ActivityType.UPDATE, Source.DEMO_REQUEST,
                String.valueOf(oldRequest.getRequestId()), oldRequest, demoRequest);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ResponseEntity<?> getDemoRequestStatus() {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        List<DemoRequestStatusResponse> responseList = Arrays.stream(RequestStatus.values())
                .map(requestStatus -> new DemoRequestStatusResponse(
                        requestStatus.name(),
                        requestStatus.getValue()
                )).toList();

        return new ResponseEntity<>(responseList, HttpStatus.OK);
    }
}
