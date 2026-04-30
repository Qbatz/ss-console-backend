package com.smartstay.console.services;

import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.Agent;
import com.smartstay.console.dao.HostelRelationalAgent;
import com.smartstay.console.dao.HostelV1;
import com.smartstay.console.dao.hostelRelationalAgent.HostelRelationalAgentSnapshot;
import com.smartstay.console.ennum.ActivityType;
import com.smartstay.console.ennum.ModuleId;
import com.smartstay.console.ennum.RelationalAgentReason;
import com.smartstay.console.ennum.Source;
import com.smartstay.console.payloads.hostelRelationalAgent.HostelRelationalAgentPayload;
import com.smartstay.console.repositories.HostelRelationalAgentRepository;
import com.smartstay.console.responses.hostelRelationalAgent.RelationalAgentReasonsRes;
import com.smartstay.console.utils.SnapshotUtility;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
public class HostelRelationalAgentService {

    @Autowired
    private HostelRelationalAgentRepository hostelRelationalAgentRepository;
    @Autowired
    private Authentication authentication;
    @Autowired
    private AgentService agentService;
    @Autowired
    private AgentRolesService agentRolesService;
    @Autowired
    private HostelService hostelService;
    @Autowired
    private AgentActivitiesService agentActivitiesService;

    public ResponseEntity<?> assignHostelRelationalAgent(String hostelId, HostelRelationalAgentPayload payload) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Hostels.getId(), Utils.PERMISSION_UPDATE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Agents.getId(), Utils.PERMISSION_UPDATE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        HostelV1 hostel = hostelService.getHostelInfo(hostelId);
        if (hostel == null) {
            return new ResponseEntity<>(Utils.INVALID_HOSTEL_ID, HttpStatus.BAD_REQUEST);
        }

        Agent payloadAgent = agentService.findUserByUserId(payload.agentId());
        if (payloadAgent == null) {
            return new ResponseEntity<>(Utils.NO_AGENT_FOUND, HttpStatus.BAD_REQUEST);
        }

        RelationalAgentReason reasonEnum;

        try {
            reasonEnum = RelationalAgentReason.valueOf(payload.reason());
        } catch (IllegalArgumentException | NullPointerException e) {
            return new ResponseEntity<>("Invalid reason: " + payload.reason(), HttpStatus.BAD_REQUEST);
        }

        HostelRelationalAgent hostelRelationalAgent = new HostelRelationalAgent();

        hostelRelationalAgent.setHostelId(hostelId);
        hostelRelationalAgent.setAgentId(payloadAgent.getAgentId());
        hostelRelationalAgent.setReason(reasonEnum);
        hostelRelationalAgent.setComments(payload.comments());
        hostelRelationalAgent.setCreatedBy(authentication.getName());
        hostelRelationalAgent.setCreatedAt(new Date());

        hostelRelationalAgent = hostelRelationalAgentRepository.save(hostelRelationalAgent);

        HostelRelationalAgentSnapshot newSnapshot = SnapshotUtility.toSnapshot(hostelRelationalAgent);

        agentActivitiesService.createAgentActivity(agent, ActivityType.CREATE, Source.HOSTEL_RELATIONAL_AGENT,
                String.valueOf(hostelRelationalAgent.getId()), null, newSnapshot);

        return new ResponseEntity<>(Utils.CREATED, HttpStatus.OK);
    }

    public ResponseEntity<?> getHostelRelationalAgentReasons() {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Hostels.getId(), Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Agents.getId(), Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        List<RelationalAgentReasonsRes> responseList = Arrays.stream(RelationalAgentReason.values())
                .map(reason -> new RelationalAgentReasonsRes(reason.name(), reason.getLabel()))
                .toList();

        return new ResponseEntity<>(responseList, HttpStatus.OK);
    }

    public List<HostelRelationalAgent> getByHostelIds(Set<String> hostelIds) {
        return hostelRelationalAgentRepository.findAllByHostelIdInOrderByIdDesc(hostelIds);
    }

    public List<HostelRelationalAgent> getByHostelId(String hostelId) {
        return hostelRelationalAgentRepository.findAllByHostelIdOrderByIdDesc(hostelId);
    }

    public List<HostelRelationalAgent> getByAgentId(String agentId) {
        return hostelRelationalAgentRepository.findAllByAgentIdOrderByIdDesc(agentId);
    }
}
