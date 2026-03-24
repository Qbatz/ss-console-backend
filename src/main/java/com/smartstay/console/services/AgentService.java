package com.smartstay.console.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartstay.console.Mapper.agent.AgentActivitiesResMapper;
import com.smartstay.console.Mapper.agent.AgentDetailsResMapper;
import com.smartstay.console.Mapper.agent.AgentResMapper;
import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.Agent;
import com.smartstay.console.dao.AgentActivities;
import com.smartstay.console.dao.AgentRoles;
import com.smartstay.console.dto.agent.RoleCountProjection;
import com.smartstay.console.dto.zoho.ZohoUserDetails;
import com.smartstay.console.ennum.ActivityType;
import com.smartstay.console.ennum.ModuleId;
import com.smartstay.console.ennum.Source;
import com.smartstay.console.payloads.AddAdmin;
import com.smartstay.console.payloads.agent.AddMockAgent;
import com.smartstay.console.repositories.AgentRepository;
import com.smartstay.console.responses.agents.*;
import com.smartstay.console.utils.Constants;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

        Agent newAgent = new Agent();
        newAgent.setAgentEmailId(addAdmin.emailId());
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

        agentActivitiesService.createAgentActivity(agents, ActivityType.CREATE, Source.AGENT,
                newAgent.getAgentId(), null, newAgent);

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

        agentActivitiesService.createAgentActivity(agent, ActivityType.CREATE, Source.MOCK_AGENT,
                newAgent.getAgentId(), null, newAgent);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    public ResponseEntity<?> getAllAgents() {

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
                .findAllByIsMockAgentFalseAndAgentIdNotOrderByCreatedAtDesc(agent.getAgentId());

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

        List<AgentResponse> activeAgents = agents.stream()
                .filter(a -> a.getIsActive() == true)
                .map(a -> new AgentResMapper(
                        rolesMap.get(a.getRoleId()),
                        agentActivitiesMap.get(a.getAgentId())
                ).apply(a))
                .toList();

        List<AgentResponse> inActiveAgents = agents.stream()
                .filter(a -> a.getIsActive() == false)
                .map(a -> new AgentResMapper(
                        rolesMap.get(a.getRoleId()),
                        agentActivitiesMap.get(a.getAgentId())
                ).apply(a))
                .toList();

        AgentListRes response = new AgentListRes(activeAgents, inActiveAgents);

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
        Agent oldAgent = new ObjectMapper().convertValue(deactivatingAgent, Agent.class);
        if (deactivatingAgent == null){
            return new ResponseEntity<>(Utils.NO_AGENT_FOUND, HttpStatus.BAD_REQUEST);
        }

        deactivatingAgent.setIsActive(false);
        deactivatingAgent.setUpdatedBy(agent.getAgentId());
        deactivatingAgent.setUpdatedAt(new Date());
        deactivatingAgent = agentRepository.save(deactivatingAgent);

        agentActivitiesService.createAgentActivity(agent, ActivityType.UPDATE, Source.AGENT,
                agentId, oldAgent, deactivatingAgent);

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
        Agent oldAgent = new ObjectMapper().convertValue(reactivatingAgent, Agent.class);
        if (reactivatingAgent == null){
            return new ResponseEntity<>(Utils.NO_AGENT_FOUND, HttpStatus.BAD_REQUEST);
        }

        reactivatingAgent.setIsActive(true);
        reactivatingAgent.setUpdatedBy(agent.getAgentId());
        reactivatingAgent.setUpdatedAt(new Date());
        reactivatingAgent = agentRepository.save(reactivatingAgent);

        agentActivitiesService.createAgentActivity(agent, ActivityType.UPDATE, Source.AGENT,
                agentId, oldAgent, reactivatingAgent);

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

        Set<String> agentIds = agentActivities.stream()
                .map(AgentActivities::getAgentId)
                .collect(Collectors.toSet());

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

        AgentDetailsRes response = new AgentDetailsResMapper(
                agentActivitiesRes, agentRole, createdBy, updatedBy
        ).apply(inputAgent);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
