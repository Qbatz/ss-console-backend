package com.smartstay.console.services;

import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.Agent;
import com.smartstay.console.dao.AgentRoles;
import com.smartstay.console.dto.zoho.ZohoUserDetails;
import com.smartstay.console.payloads.AddAdmin;
import com.smartstay.console.repositories.AgentRepository;
import com.smartstay.console.utils.Constants;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

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
            return new ResponseEntity<>(Constants.PAYLOAD_REQUIRED, HttpStatus.BAD_GATEWAY);
        }
        if (addAdmin.emailId() == null || addAdmin.emailId().trim().equalsIgnoreCase("")) {
            return new ResponseEntity<>(Constants.EMAIL_ID_REQUIRED, HttpStatus.BAD_REQUEST);
        }

        Agent newAgent = new Agent();
        newAgent.setAgentEmailId(addAdmin.emailId());
        newAgent.setIsProfileCompleted(false);

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
}
