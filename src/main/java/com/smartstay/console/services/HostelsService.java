package com.smartstay.console.services;

import com.smartstay.console.Mapper.hostels.HostelDetailsMapper;
import com.smartstay.console.Mapper.hostels.HostelsListMapper;
import com.smartstay.console.Mapper.users.UserOnerInfoMapper;
import com.smartstay.console.Mapper.users.UsersResponseMapper;
import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.*;
import com.smartstay.console.ennum.ModuleId;
import com.smartstay.console.repositories.HostelV1Repositories;
import com.smartstay.console.responses.hostels.HostelList;
import com.smartstay.console.responses.hostels.HostelResponse;
import com.smartstay.console.responses.hostels.Hostels;
import com.smartstay.console.responses.hostels.OwnerInfo;
import com.smartstay.console.responses.users.UsersResponse;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    @Autowired
    private FloorsService floorsService;
    @Autowired
    private RoomsService roomsService;
    @Autowired
    private BedsService bedsService;
    @Autowired
    private CustomersService customersService;
    @Autowired
    private AddressService addressService;

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

    public List<HostelV1> getHostelsByParentIds(List<String> parentIds) {
        return hostelRepository.findAllByParentIdIn(parentIds);
    }

    public ResponseEntity<?> getHostelByHostelId(String hostelId) {

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

        HostelV1 hostel = hostelRepository.findByHostelId(hostelId);
        if (hostel == null){
            return new ResponseEntity<>(Utils.NO_HOSTEL_FOUND, HttpStatus.BAD_REQUEST);
        }

        Users owner = usersService.getOwner(hostel.getParentId());
        OwnerInfo ownerInfo = new UserOnerInfoMapper().apply(owner);

        List<Users> masters = usersService.getMasters(hostel);
        List<Address> mastersAddressList = addressService.getAddressByUsers(masters);
        Map<Users, Address> mastersAddressMap = mastersAddressList.stream()
                .collect(Collectors.toMap(Address::getUser, address -> address));
        List<UsersResponse> mastersRes = masters.stream()
                .map(users -> new UsersResponseMapper(
                        mastersAddressMap.get(users)
                ).apply(users)).toList();

        List<Users> staffs = usersService.getStaffs(hostel);
        List<Address> staffsAddressList = addressService.getAddressByUsers(staffs);
        Map<Users, Address> staffsAddressMap = staffsAddressList.stream()
                .collect(Collectors.toMap(Address::getUser, address -> address));
        List<UsersResponse> staffsRes = staffs.stream()
                .map(users -> new UsersResponseMapper(
                        staffsAddressMap.get(users)
                ).apply(users)).toList();

        int noOfFloors = floorsService.getCountByHostelId(hostelId);
        int noOfRooms = roomsService.getCountByHostelId(hostelId);
        int noOfBeds = bedsService.getCountByHostelId(hostelId);
        int noOfTenants = customersService.getCountByHostelId(hostelId);

        List<Subscription> subscriptions = subscriptionService.getSubscriptionsByHostelId(hostelId);

        HostelResponse hostelDetails = new HostelDetailsMapper(
                ownerInfo, noOfFloors, noOfRooms, noOfBeds, noOfTenants,
                subscriptions, mastersRes, staffsRes
        ).apply(hostel);

        return new ResponseEntity<>(hostelDetails, HttpStatus.OK);
    }
}
