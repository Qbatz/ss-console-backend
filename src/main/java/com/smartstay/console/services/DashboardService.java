package com.smartstay.console.services;

import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.Agent;
import com.smartstay.console.ennum.ModuleId;
import com.smartstay.console.responses.dashboard.DashboardResponse;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    @Autowired
    private Authentication authentication;
    @Autowired
    private HostelsService hostelsService;
    @Autowired
    private OwnersService ownersService;
    @Autowired
    private AgentService agentService;
    @Autowired
    private DemoRequestService demoRequestService;
    @Autowired
    private SubscriptionService subscriptionService;
    @Autowired
    private AgentRolesService agentRolesService;

    public ResponseEntity<?> getDashboard() {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        long hostelCount = 0;
        long ownersCount = 0;
        long agentCount = 0;
        long demoRequestCount = 0;
        long expiredSubscriptionsCount = 0;

        if (agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Hostels.getId(), Utils.PERMISSION_READ)) {
            hostelCount = hostelsService.getHostelCount();
        }
        if (agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Owners.getId(), Utils.PERMISSION_READ)) {
            ownersCount = ownersService.getOwnerCount();
        }
        if (agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Agents.getId(), Utils.PERMISSION_READ)) {
            agentCount = agentService.getAgentCount();
        }
        demoRequestCount = demoRequestService.getDemoRequestCount();
        if (agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Subscriptions.getId(), Utils.PERMISSION_READ)) {
            expiredSubscriptionsCount = subscriptionService.getExpiredSubscriptionsCount();
        }

        DashboardResponse response = new DashboardResponse(hostelCount,  ownersCount, agentCount,
                demoRequestCount, expiredSubscriptionsCount);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
