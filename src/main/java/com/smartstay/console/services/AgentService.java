package com.smartstay.console.services;

import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.Agent;
import com.smartstay.console.dao.AgentRoles;
import com.smartstay.console.dto.agent.RoleCountProjection;
import com.smartstay.console.dto.zoho.ZohoUserDetails;
import com.smartstay.console.payloads.AddAdmin;
import com.smartstay.console.payloads.agent.AddMockAgent;
import com.smartstay.console.repositories.AgentRepository;
import com.smartstay.console.responses.agents.AgentDetails;
import com.smartstay.console.utils.Constants;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AgentService {

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private AgentRolesService agentRolesService;
    @Autowired
    private Authentication authentication;

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
        Agent agents = agentRepository.findByAgentId(authentication.getName());
        if (agents == null) {
            return new ResponseEntity<>(Constants.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (addAdmin == null) {
            return new ResponseEntity<>(Constants.PAYLOAD_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        if (addAdmin.emailId() == null || addAdmin.emailId().trim().equalsIgnoreCase("")) {
            return new ResponseEntity<>(Constants.EMAIL_ID_REQUIRED, HttpStatus.BAD_REQUEST);
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
        agentRepository.save(newAgent);

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
        Agent agent = agentRepository.findByAgentId(authentication.getName());
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

        AgentDetails agentDetails = new AgentDetails(fullName.toString(),
                initials.toString(),
                agent.getAgentEmailId(),
                agent.getFirstName(),
                agent.getLastName(),
                agent.getMobile());

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

        Agent newAgent = new Agent();

        String email = addMockAgent.email().trim();
        Long roleId = addMockAgent.roleId();

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

        agentRepository.save(newAgent);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
