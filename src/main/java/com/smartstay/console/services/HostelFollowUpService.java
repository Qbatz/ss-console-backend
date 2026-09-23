package com.smartstay.console.services;

import com.smartstay.console.Mapper.hostelFollowUp.HostelFollowUpResMapper;
import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.Agent;
import com.smartstay.console.dao.HostelFollowUp;
import com.smartstay.console.dao.HostelV1;
import com.smartstay.console.dto.hostelFollowUp.HostelFollowUpSnapshot;
import com.smartstay.console.ennum.*;
import com.smartstay.console.payloads.hostelFollowUp.HostelFollowUpPayload;
import com.smartstay.console.repositories.HostelFollowUpRepository;
import com.smartstay.console.responses.hostelFollowUp.HostelFollowUpResponse;
import com.smartstay.console.responses.hostelFollowUp.HostelFollowUpStatusRes;
import com.smartstay.console.utils.SnapshotUtility;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class HostelFollowUpService {

    @Autowired
    private HostelFollowUpRepository hostelFollowUpRepository;
    @Autowired
    private HostelService hostelService;
    @Autowired
    private Authentication authentication;
    @Autowired
    private AgentService agentService;
    @Autowired
    private AgentRolesService agentRolesService;
    @Autowired
    private AgentActivitiesService agentActivitiesService;

    public ResponseEntity<?> getHostelFollowUpByHostelId(String hostelId) {

        String loggedInAgentId = authentication.getName();
        Agent loggedInAgent = agentService.findUserByUserId(loggedInAgentId);
        if (loggedInAgent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(loggedInAgent.getRoleId(), ModuleId.Hostels.getId(), Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        HostelV1 hostel = hostelService.getHostelInfo(hostelId);
        if (hostel == null){
            return new ResponseEntity<>(Utils.NO_HOSTEL_FOUND, HttpStatus.BAD_REQUEST);
        }

        HostelFollowUp latestFollowUp = hostelFollowUpRepository
                .findTopByHostelIdOrderByFollowUpIdDesc(hostelId);

        List<HostelFollowUp> hostelFollowUps = hostelFollowUpRepository
                .findAllByHostelIdOrderByFollowUpIdDesc(hostelId);

        Set<String> agentIds = new HashSet<>();

        for (HostelFollowUp followUp : hostelFollowUps) {
            if (followUp.getCreatedBy() != null){
                agentIds.add(followUp.getCreatedBy());
            }
        }

        List<Agent> agents = agentService.getAgentsByIds(agentIds);
        Map<String, Agent> agentMap = agents.stream()
                .collect(Collectors.toMap(Agent::getAgentId, Function.identity()));

        HostelFollowUpResMapper mapper = new HostelFollowUpResMapper(hostelFollowUps, agentMap);

        HostelFollowUpResponse response = mapper.apply(latestFollowUp);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> updateHostelFollowUpStatus(HostelFollowUpPayload payload) {

        String loggedInAgentId = authentication.getName();
        Agent loggedInAgent = agentService.findUserByUserId(loggedInAgentId);
        if (loggedInAgent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(loggedInAgent.getRoleId(), ModuleId.Hostels.getId(), Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        String hostelId = payload.hostelId();
        HostelV1 hostel = hostelService.getHostelInfo(hostelId);
        if (hostel == null){
            return new ResponseEntity<>(Utils.NO_HOSTEL_FOUND, HttpStatus.BAD_REQUEST);
        }

        HostelFollowUpStatusEnum followUpStatus;
        String newStatus = payload.status();
        try {
            followUpStatus = HostelFollowUpStatusEnum.valueOf(newStatus);
        } catch (Exception e) {
            return new ResponseEntity<>(Utils.HOSTEL_FOLLOW_UP_STATUS_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        Date today = new Date();

        HostelFollowUp hostelFollowUp = new HostelFollowUp();

        hostelFollowUp.setHostelId(hostelId);
        hostelFollowUp.setComments(payload.comments());
        hostelFollowUp.setStatus(newStatus);
        hostelFollowUp.setCreatedBy(loggedInAgentId);
        hostelFollowUp.setCreatedAt(today);

        String reason = payload.reason();

        if (!followUpStatus.getReasons().contains(reason)) {
            return new ResponseEntity<>(Utils.REASON_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        hostelFollowUp.setReason(reason);

        hostelFollowUp = hostelFollowUpRepository.save(hostelFollowUp);

        HostelFollowUpSnapshot newSnapshot = SnapshotUtility.toSnapshot(hostelFollowUp);

        agentActivitiesService.createAgentActivity(loggedInAgent, ActivityType.CREATE, Source.HOSTEL_FOLLOW_UP,
                String.valueOf(hostelFollowUp.getFollowUpId()), null, newSnapshot);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ResponseEntity<?> getHostelFollowUpStatus() {

        List<HostelFollowUpStatusRes> response = Arrays.stream(HostelFollowUpStatusEnum.values())
                .map(i -> new HostelFollowUpStatusRes(i.name(), i.getValue(),
                        i.getReasons()))
                .toList();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
