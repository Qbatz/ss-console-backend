package com.smartstay.console.services;

import com.smartstay.console.Mapper.hostels.HostelsListMapper;
import com.smartstay.console.Mapper.users.UserOnerInfoMapper;
import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.Agent;
import com.smartstay.console.dao.HostelV1;
import com.smartstay.console.dao.Subscription;
import com.smartstay.console.dao.Users;
import com.smartstay.console.ennum.ModuleId;
import com.smartstay.console.repositories.HostelV1Repositories;
import com.smartstay.console.responses.hostels.HostelList;
import com.smartstay.console.responses.hostels.OwnerInfo;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HostelsService {
    @Autowired
    private Authentication authentication;
    @Autowired
    private AgentService agentService;
    @Autowired
    private AgentRolesService agentRolesService;
    @Autowired
    private UsersService usersService;
    @Autowired
    private SubscriptionService subscriptionService;
    @Autowired
    private HostelV1Repositories hostelRepository;

    public ResponseEntity<?> getAllHostels() {
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

        List<HostelV1> listHostels = hostelRepository.findAll();
        List<String> parentId = listHostels
                .stream()
                .map(HostelV1::getParentId)
                .toList();

        List<Users> createdUsers = usersService.getOwners(parentId);
        List<OwnerInfo> ownerInfos = createdUsers
                .stream()
                .map(i -> new UserOnerInfoMapper().apply(i))
                .toList();

        List<HostelList> hostels = listHostels
                .stream()
                .map(i -> new HostelsListMapper(ownerInfos).apply(i))
                .toList();

        return new ResponseEntity<>(hostels, HttpStatus.OK);

    }
}
