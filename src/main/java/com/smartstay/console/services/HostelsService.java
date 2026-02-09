package com.smartstay.console.services;

import com.smartstay.console.Mapper.hostels.HostelsListMapper;
import com.smartstay.console.Mapper.users.UserOnerInfoMapper;
import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.*;
import com.smartstay.console.ennum.ModuleId;
import com.smartstay.console.repositories.HostelV1Repositories;
import com.smartstay.console.responses.hostels.HostelList;
import com.smartstay.console.responses.hostels.Hostels;
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
    private HostelPlanService hostelPlansService;
    @Autowired
    private SubscriptionService subscriptionService;
    @Autowired
    private UserActivitiesService userActivitiesService;
    @Autowired
    private HostelV1Repositories hostelRepository;

    public ResponseEntity<?> getAllHostels(int page, int size, String hostelName) {
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
        if (page > 0) {
            page = page - 1;
        }

        long totalHostels = hostelRepository.findHostelCount();

        int currentPage = page;

        List<HostelPlan> actHostels = hostelPlansService.findActiveHostels();


        List<HostelV1> allHostels = hostelRepository.findAllHostels(size, page*size, hostelName);

        List<HostelV1> hostelsFromPlan = allHostels
                .stream()
                .toList();

        List<String> parentId = hostelsFromPlan
                .stream()
                .map(HostelV1::getParentId)
                .toList();
        List<String> hostelIds = hostelsFromPlan
                .stream()
                .map(HostelV1::getHostelId)
                .toList();

        List<Users> createdUsers = usersService.getOwners(parentId);
        List<OwnerInfo> ownerInfos = createdUsers
                .stream()
                .map(i -> new UserOnerInfoMapper().apply(i))
                .toList();
        List<UserActivities> listActivities = userActivitiesService.findLatestActivities(hostelIds);

        List<HostelList> hostelsList = hostelsFromPlan
                .stream()
                .map(i -> new HostelsListMapper(ownerInfos, listActivities).apply(i))
                .toList();

        long inactiveHostels = totalHostels - actHostels.size();

        Hostels hostels = new Hostels(totalHostels,
                actHostels.size(),
                inactiveHostels,
                currentPage,
                size,
                hostelsList);

        return new ResponseEntity<>(hostels, HttpStatus.OK);

    }
}
