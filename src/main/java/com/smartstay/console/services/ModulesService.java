package com.smartstay.console.services;

import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.Agent;
import com.smartstay.console.dao.AgentModules;
import com.smartstay.console.ennum.ModuleId;
import com.smartstay.console.repositories.AgentModulesRepository;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ModulesService {
    @Autowired
    private Authentication authentication;
    @Autowired
    private AgentRolesService agentRolesService;
    @Autowired
    private AgentService agentService;
    @Autowired
    private AgentModulesRepository modulesRepository;

    public ResponseEntity<?> getAllModules() {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        List<AgentModules> agentModules = modulesRepository.findAll();

        return new ResponseEntity<>(agentModules, HttpStatus.OK);
    }
}
