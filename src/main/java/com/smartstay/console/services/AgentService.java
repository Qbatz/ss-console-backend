package com.smartstay.console.services;

import com.smartstay.console.Mapper.agent.AgentActivitiesResMapper;
import com.smartstay.console.Mapper.agent.AgentDetailsResMapper;
import com.smartstay.console.Mapper.agent.AgentResMapper;
import com.smartstay.console.Mapper.hostelRelationalAgent.RelationalAgentResMapper;
import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.*;
import com.smartstay.console.dto.agent.AgentSnapshot;
import com.smartstay.console.dto.agent.RoleCountProjection;
import com.smartstay.console.dto.zoho.ZohoUserDetails;
import com.smartstay.console.ennum.ActivityType;
import com.smartstay.console.ennum.ModuleId;
import com.smartstay.console.ennum.Source;
import com.smartstay.console.payloads.AddAdmin;
import com.smartstay.console.payloads.agent.AddMockAgent;
import com.smartstay.console.payloads.roles.RoleIdPayload;
import com.smartstay.console.repositories.AgentRepository;
import com.smartstay.console.responses.agents.*;
import com.smartstay.console.responses.hostelRelationalAgent.RelationalAgentResponse;
import com.smartstay.console.utils.Constants;
import com.smartstay.console.utils.SnapshotUtility;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AgentService {

    @Autowired
    private AgentRepository agentRepository;
    @Autowired
    private AgentRolesService agentRolesService;
    @Autowired
    private Authentication authentication;
    @Autowired
    private AgentActivitiesService agentActivitiesService;
    @Autowired
    @Lazy
    private HostelRelationalAgentService hostelRelationalAgentService;
    @Autowired
    private HostelService hostelService;
    @Lazy
    @Autowired
    private UsersService usersService;

    public Agent findAgentByEmail(String agentEmail) {
        return agentRepository.findByAgentEmailId(agentEmail);
    }

    public Agent updateProfileFromLogin(Agent agents, ZohoUserDetails userDetails) {
        agents.setFirstName(userDetails.firstName());
        agents.setLastName(userDetails.lastName());
        agents.setAgentZohoUserId(userDetails.zohoId());
        agents.setIsProfileCompleted(true);

        return agentRepository.save(agents);
    }

    public ResponseEntity<?> addAdmin(AddAdmin addAdmin) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Constants.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agents = agentRepository.findByAgentIdAndIsActiveTrue(authentication.getName());
        if (agents == null) {
            return new ResponseEntity<>(Constants.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (addAdmin == null) {
            return new ResponseEntity<>(Constants.PAYLOAD_REQUIRED, HttpStatus.BAD_REQUEST);
        }

        if (addAdmin.emailId() == null || addAdmin.emailId().trim().equalsIgnoreCase("")) {
            return new ResponseEntity<>(Constants.EMAIL_ID_REQUIRED, HttpStatus.BAD_REQUEST);
        }

        if (agentRepository.existsByAgentEmailIdAndIsMockAgentFalse(addAdmin.emailId())){
            return new ResponseEntity<>(Utils.EMAIL_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
        }

        String email = addAdmin.emailId().trim();

        int atIndex = email.indexOf("@");
        String firstName = atIndex > 0 ? email.substring(0, atIndex) : email;

        Agent newAgent = new Agent();
        newAgent.setFirstName(firstName);
        newAgent.setAgentEmailId(email);
        newAgent.setIsProfileCompleted(false);
        newAgent.setIsActive(true);
        newAgent.setCreatedAt(new Date());
        newAgent.setCreatedBy(authentication.getName());
        newAgent.setTicketLink(addAdmin.ticketLink());

        if (addAdmin.roleId() != null) {
            AgentRoles role = agentRolesService.findById(addAdmin.roleId());
            if (role == null) {
                return new ResponseEntity<>(Utils.NO_ROLES_FOUND, HttpStatus.BAD_REQUEST);
            }
            newAgent.setRoleId(agents.getRoleId());

        }
        newAgent = agentRepository.save(newAgent);

        AgentSnapshot newAgentSnapshot = SnapshotUtility.toSnapshot(newAgent);

        agentActivitiesService.createAgentActivity(agents, ActivityType.CREATE, Source.AGENT,
                newAgent.getAgentId(), null, newAgentSnapshot);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    public Agent findUserByUserId(String userId) {
        return agentRepository.findByAgentIdAndIsActiveTrue(userId);
    }

    public List<Agent> findActiveUsersByRoleId(long roleId) {
        return agentRepository.findByRoleIdAndIsActiveTrue(roleId);
    }

    public ResponseEntity<?> getAgentDetails() {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentRepository.findByAgentIdAndIsActiveTrue(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        StringBuilder fullName = new StringBuilder();
        StringBuilder initials = new StringBuilder();
        if (agent.getFirstName() != null) {
            fullName.append(agent.getFirstName());
            initials.append(agent.getFirstName().trim().toUpperCase().charAt(0));
        }
        if (agent.getLastName() != null && !agent.getLastName().trim().equalsIgnoreCase("")) {
            fullName.append(" ");
            fullName.append(agent.getLastName());
            initials.append(agent.getLastName().trim().toUpperCase().charAt(0));
        }
        else {
            if (agent.getFirstName() != null) {
                String[] nameArr = agent.getFirstName().split(" ");
                if (nameArr.length > 1) {
                    initials.append(nameArr[nameArr.length - 1].trim().toUpperCase().charAt(0));
                }
                else {
                    if (!nameArr[0].isEmpty()) {
                        initials.append(nameArr[0].toUpperCase().charAt(1));
                    }
                }
            }
        }

        AgentRoles agentRoles = agentRolesService.getAgentRoleById(agent.getRoleId());

        AgentDetails agentDetails = new AgentDetails(fullName.toString(),
                initials.toString(),
                agent.getAgentEmailId(),
                agent.getFirstName(),
                agent.getLastName(),
                agent.getMobile(),
                agent.getRoleId(),
                agentRoles.getRoleName(),
                agentRoles.getPermissions());

        return new ResponseEntity<>(agentDetails, HttpStatus.OK);
    }

    public Map<Long,Long> findCountOfAgentByRoleIds(List<Long> roleIds){
        return agentRepository.countActiveAgentsByRoleIds(roleIds)
                .stream().collect(Collectors.toMap(
                        RoleCountProjection::getRoleId,
                        RoleCountProjection::getCount
                ));
    }

    public ResponseEntity<?> addMockAgent(AddMockAgent addMockAgent) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Constants.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentRepository.findByAgentIdAndIsActiveTrue(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Constants.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent newAgent = new Agent();

        String email = addMockAgent.email().trim();
        Long roleId = addMockAgent.roleId();

        if (agentRepository.existsByAgentEmailId(email)){
            return new ResponseEntity<>(Utils.EMAIL_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
        }

        int atIndex = email.indexOf("@");
        String firstName = atIndex > 0 ? email.substring(0, atIndex) : email;

        newAgent.setFirstName(firstName);
        newAgent.setAgentEmailId(email);

        AgentRoles role = agentRolesService.findById(roleId);
        if (role == null) {
            return new ResponseEntity<>(Utils.NO_ROLES_FOUND, HttpStatus.BAD_REQUEST);
        }
        newAgent.setRoleId(roleId);

        newAgent.setMockAgent(true);

        newAgent.setIsActive(true);
        newAgent.setIsProfileCompleted(false);
        newAgent.setCreatedAt(new Date());
        newAgent.setCreatedBy(authentication.getName());

        newAgent = agentRepository.save(newAgent);

        AgentSnapshot newAgentSnapshot = SnapshotUtility.toSnapshot(newAgent);

        agentActivitiesService.createAgentActivity(agent, ActivityType.CREATE, Source.MOCK_AGENT,
                newAgent.getAgentId(), null, newAgentSnapshot);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    public ResponseEntity<?> getAllAgents(String name, boolean isActive,
                                          Long roleId, int page, int size) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentRepository.findByAgentIdAndIsActiveTrue(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Agents.getId(), Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        page = Math.max(page - 1, 0);
        size = Math.max(size, 1);
        name = name == null || name.isBlank() ? null : name.trim();

        Pageable pageable = PageRequest.of(page, size);

        Page<Agent> pagedAgents = agentRepository
                .findPaginatedAgents(name, isActive, roleId, pageable);

        List<Agent> agents = pagedAgents.getContent();

        Set<Long> roleIds = agents.stream()
                .map(Agent::getRoleId)
                .collect(Collectors.toSet());
        List<AgentRoles> roles = agentRolesService.getAgentRolesByRoleIds(roleIds);
        Map<Long, AgentRoles> rolesMap = roles.stream()
                .collect(Collectors.toMap(AgentRoles::getRoleId, role -> role));

        Set<String> agentIds = agents.stream()
                .map(Agent::getAgentId)
                .collect(Collectors.toSet());
        List<AgentActivities> agentActivities = agentActivitiesService
                .getLatestActivityByAgentIds(agentIds);
        Map<String, AgentActivities> agentActivitiesMap = agentActivities.stream()
                .collect(Collectors.toMap(AgentActivities::getAgentId,
                        agentAct -> agentAct));

        List<AgentResponse> agentList = agents.stream()
                .map(a -> new AgentResMapper(
                        rolesMap.get(a.getRoleId()),
                        agentActivitiesMap.get(a.getAgentId()),
                        agent
                ).apply(a))
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("agentList", agentList);
        response.put("currentPage", page + 1);
        response.put("pageSize", size);
        response.put("totalItems", pagedAgents.getTotalElements());
        response.put("totalPages", pagedAgents.getTotalPages());
        response.put("isActive", isActive);
        response.put("roleId", roleId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> deactivateAgent(String agentId) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentRepository.findByAgentIdAndIsActiveTrue(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Agents.getId(), Utils.PERMISSION_UPDATE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        Agent deactivatingAgent = agentRepository.findByAgentIdAndIsActiveTrue(agentId);
        if (deactivatingAgent == null){
            return new ResponseEntity<>(Utils.NO_AGENT_FOUND, HttpStatus.BAD_REQUEST);
        }

        if (agent.getAgentId().equals(agentId)) {
            return new ResponseEntity<>(Utils.CANNOT_EDIT_YOURSELF, HttpStatus.BAD_REQUEST);
        }

        AgentSnapshot oldAgent = SnapshotUtility.toSnapshot(deactivatingAgent);

        deactivatingAgent.setIsActive(false);
        deactivatingAgent.setUpdatedBy(agent.getAgentId());
        deactivatingAgent.setUpdatedAt(new Date());

        agentRepository.save(deactivatingAgent);

        agentActivitiesService.createAgentActivity(agent, ActivityType.DEACTIVATE, Source.AGENT,
                agentId, oldAgent, null);

        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);
    }

    public List<Agent> getAgentsByIds(Set<String> agentIds) {
        return agentRepository.findAllByAgentIdInAndIsActiveTrue(agentIds);
    }

    public long getAgentCount(){
        return agentRepository.getCount();
    }

    public ResponseEntity<?> getAgentsDropdown() {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentRepository.findByAgentIdAndIsActiveTrue(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Agents.getId(), Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        List<Agent> agents = agentRepository
                .findAllByIsMockAgentFalseAndIsActiveTrueOrderByCreatedAtDesc();

        List<AgentDropdown> response = agents.stream()
                .map(a -> new AgentDropdown(a.getAgentId(),
                        Utils.getFullName(a.getFirstName(), a.getLastName())))
                .toList();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> reactivateAgent(String agentId) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentRepository.findByAgentIdAndIsActiveTrue(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Agents.getId(), Utils.PERMISSION_UPDATE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        Agent reactivatingAgent = agentRepository.findByAgentIdAndIsActiveFalse(agentId);
        if (reactivatingAgent == null){
            return new ResponseEntity<>(Utils.NO_AGENT_FOUND, HttpStatus.BAD_REQUEST);
        }

        if (agent.getAgentId().equals(agentId)) {
            return new ResponseEntity<>(Utils.CANNOT_EDIT_YOURSELF, HttpStatus.BAD_REQUEST);
        }

        reactivatingAgent.setIsActive(true);
        reactivatingAgent.setUpdatedBy(agent.getAgentId());
        reactivatingAgent.setUpdatedAt(new Date());
        reactivatingAgent = agentRepository.save(reactivatingAgent);

        AgentSnapshot reactivatingAgentSnapshot = SnapshotUtility.toSnapshot(reactivatingAgent);

        agentActivitiesService.createAgentActivity(agent, ActivityType.REACTIVATE, Source.AGENT,
                agentId, null, reactivatingAgentSnapshot);

        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);
    }

    public ResponseEntity<?> getAgentDetailsByAgentId(String agentId) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentRepository.findByAgentIdAndIsActiveTrue(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Agents.getId(), Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        Agent inputAgent = agentRepository.findByAgentId(agentId);
        if (inputAgent == null){
            return new ResponseEntity<>(Utils.NO_AGENT_FOUND, HttpStatus.BAD_REQUEST);
        }

        List<AgentActivities> agentActivities = agentActivitiesService
                .getLimitedActivitiesByAgentId(agentId, 50);
        List<HostelRelationalAgent> hostelRelations = hostelRelationalAgentService
                .getByAgentId(agentId);

        Set<String> agentIds = new HashSet<>();
        for (AgentActivities a : agentActivities) {
            if (a.getAgentId() != null){
                agentIds.add(a.getAgentId());
            }
        }

        for (HostelRelationalAgent hostelRelational : hostelRelations) {
            if (hostelRelational.getAgentId() != null){
                agentIds.add(hostelRelational.getAgentId());
            }
            if (hostelRelational.getCreatedBy() != null){
                agentIds.add(hostelRelational.getCreatedBy());
            }
        }

        List<Agent> agents = agentRepository
                .findAllByAgentIdIn(agentIds);

        Map<String, Agent> agentMap = agents.stream()
                .collect(Collectors.toMap(Agent::getAgentId, a -> a));

        List<AgentActivitiesRes> agentActivitiesRes = agentActivities.stream()
                .map(agentActivity -> new AgentActivitiesResMapper(
                        agentMap.getOrDefault(agentActivity.getAgentId(), null)
                ).apply(agentActivity))
                .toList();

        AgentRoles agentRole = agentRolesService.getAgentRoleById(inputAgent.getRoleId());

        Agent createdBy = agentRepository.findByAgentId(inputAgent.getCreatedBy());
        Agent updatedBy = null;
        if (inputAgent.getUpdatedBy() != null){
            updatedBy = agentRepository.findByAgentId(inputAgent.getUpdatedBy());
        }

        Set<String> parentIds = hostelRelations.stream()
                .map(HostelRelationalAgent::getParentId)
                .collect(Collectors.toSet());

        List<Users> owners = usersService.getOwners(new ArrayList<>(parentIds));
        Map<String, Users> ownerMap = owners.stream()
                .collect(Collectors.toMap(Users::getParentId, user -> user));

        List<HostelV1> hostels = hostelService.getHostelsByParentIds(parentIds);
        Map<String, List<HostelV1>> hostelMap = hostels.stream()
                .collect(Collectors.groupingBy(HostelV1::getParentId));

        List<RelationalAgentResponse> hostelRelationsRes = hostelRelations.stream()
                .map(hostelRelationalAgent -> {
                    Users owner = ownerMap.getOrDefault(hostelRelationalAgent.getParentId(), null);
                    List<HostelV1> ownerHostels = hostelMap.getOrDefault(hostelRelationalAgent.getParentId(), null);
                    Agent relationalAgent = agentMap.getOrDefault(hostelRelationalAgent.getAgentId(), null);
                    Agent createdByAgent = agentMap.getOrDefault(hostelRelationalAgent.getCreatedBy(), null);

                    return new RelationalAgentResMapper(
                            owner, ownerHostels, relationalAgent, createdByAgent
                    ).apply(hostelRelationalAgent);
                }).toList();

        AgentDetailsRes response = new AgentDetailsResMapper(
                agentActivitiesRes, agentRole, createdBy, updatedBy, hostelRelationsRes
        ).apply(inputAgent);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> updateRoleByAgentId(String agentId, RoleIdPayload payload) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentRepository.findByAgentIdAndIsActiveTrue(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Agents.getId(), Utils.PERMISSION_UPDATE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        if (agent.getAgentId().equals(agentId)) {
            return new ResponseEntity<>(Utils.CANNOT_EDIT_YOURSELF, HttpStatus.BAD_REQUEST);
        }

        Agent updatingAgent = agentRepository.findByAgentIdAndIsActiveTrue(agentId);
        if (updatingAgent == null){
            return new ResponseEntity<>(Utils.NO_AGENT_FOUND, HttpStatus.BAD_REQUEST);
        }

        if (updatingAgent.getRoleId().equals(payload.roleId())) {
            return new ResponseEntity<>(Utils.NO_CHANGES_DETECTED, HttpStatus.BAD_REQUEST);
        }

        AgentSnapshot oldAgent = SnapshotUtility.toSnapshot(updatingAgent);

        AgentRoles agentRole = agentRolesService.getAgentRoleById(payload.roleId());
        if (agentRole == null) {
            return new ResponseEntity<>(Utils.NO_ROLES_FOUND, HttpStatus.BAD_REQUEST);
        }

        updatingAgent.setRoleId(payload.roleId());
        updatingAgent.setUpdatedBy(agent.getAgentId());
        updatingAgent.setUpdatedAt(new Date());
        updatingAgent = agentRepository.save(updatingAgent);

        AgentSnapshot newAgent = SnapshotUtility.toSnapshot(updatingAgent);

        agentActivitiesService.createAgentActivity(agent, ActivityType.UPDATE, Source.AGENT,
                agentId, oldAgent, newAgent);

        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);
    }
}
