package com.smartstay.console.services;

import com.smartstay.console.Mapper.customers.CustomerResMapper;
import com.smartstay.console.Mapper.hostels.HostelDetailsMapper;
import com.smartstay.console.Mapper.hostels.HostelsListMapper;
import com.smartstay.console.Mapper.users.UserOnerInfoMapper;
import com.smartstay.console.Mapper.users.UsersResponseMapper;
import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.*;
import com.smartstay.console.dao.HostelPlan;
import com.smartstay.console.ennum.BookingsStatus;
import com.smartstay.console.ennum.ModuleId;
import com.smartstay.console.repositories.HostelV1Repositories;
import com.smartstay.console.responses.customers.CustomerResponse;
import com.smartstay.console.responses.hostels.*;
import com.smartstay.console.responses.users.UsersResponse;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
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
    private BookingsService bookingsService;
    @Autowired
    private CustomersService customersService;
    @Autowired
    private AmenitiesService amenitiesService;

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
        Map<Users, Address> mastersAddressMap = masters.stream()
                .collect(Collectors.toMap(master -> master, Users::getAddress));
        List<UsersResponse> mastersRes = masters.stream()
                .map(users -> new UsersResponseMapper(
                        mastersAddressMap.get(users)
                ).apply(users)).toList();

        List<Users> staffs = usersService.getStaffs(hostel);
//        Map<Users, Address> staffsAddressMap = staffs.stream()
//                .collect(Collectors.toMap(staff -> staff, Users::getAddress));
        List<UsersResponse> staffsRes = staffs.stream()
                .map(users -> new UsersResponseMapper(
//                        staffsAddressMap.get(users)
                        null
                ).apply(users)).toList();

        List<Rooms> rooms = roomsService.getRoomsByHostelId(hostelId);
        List<Beds> beds = bedsService.getBedsByHostelId(hostelId);
        List<BookingsV1> bookings = bookingsService.getBookingsByHostelId(hostelId);

        Map<Integer, List<Rooms>> roomsBySharing = rooms.stream()
                .collect(Collectors.groupingBy(Rooms::getSharingType));

        Map<Integer, List<Beds>> bedsByRoom = beds.stream()
                .collect(Collectors.groupingBy(Beds::getRoomId));

        Set<String> activeStatuses = Set.of(
                BookingsStatus.CHECKIN.name(),
                BookingsStatus.BOOKED.name(),
                BookingsStatus.NOTICE.name()
        );

        Set<Integer> occupiedBedIds = bookings.stream()
                .filter(b -> activeStatuses.contains(b.getCurrentStatus()))
                .map(BookingsV1::getBedId)
                .collect(Collectors.toSet());

        List<SharingTypeResponse> sharingTypeList = new ArrayList<>();

        for (Map.Entry<Integer, List<Rooms>> entry : roomsBySharing.entrySet()) {

            Integer sharingType = entry.getKey();
            List<Rooms> sharingRooms = entry.getValue();

            int noOfRooms = sharingRooms.size();

            int noOfBeds = 0;
            int noOfOccupiedBeds = 0;
            int noOfAvailableRooms = 0;

            for (Rooms room : sharingRooms) {

                List<Beds> roomBeds = bedsByRoom.getOrDefault(room.getRoomId(), Collections.emptyList());

                int totalBedsInRoom = roomBeds.size();

                long occupiedBedsInRoom = roomBeds.stream()
                        .filter(b -> occupiedBedIds.contains(b.getBedId()))
                        .count();

                noOfBeds += totalBedsInRoom;
                noOfOccupiedBeds += (int) occupiedBedsInRoom;

                if (occupiedBedsInRoom < totalBedsInRoom) {
                    noOfAvailableRooms++;
                }
            }

            SharingTypeResponse sharingTypeResponse = new SharingTypeResponse(
                    sharingType,
                    sharingType + "-Sharing",
                    noOfRooms,
                    noOfBeds,
                    noOfOccupiedBeds,
                    noOfAvailableRooms
            );

            sharingTypeList.add(sharingTypeResponse);
        }

        sharingTypeList.sort(Comparator.comparing(SharingTypeResponse::sharingType));

        int noOfFloors = floorsService.getCountByHostelId(hostelId);
        int noOfRooms = rooms.size();
        int noOfBeds = beds.size();
        int noOfBookedTenants = 0;
        int noOfCheckedInTenants = 0;
        int noOfNoticeTenants = 0;
        int noOfVacatedTenants = 0;
        int noOfTerminatedTenants = 0;

        for (BookingsV1 booking : bookings){
            if (booking.getCurrentStatus().equalsIgnoreCase(BookingsStatus.BOOKED.name())){
                noOfBookedTenants++;
            } else if (booking.getCurrentStatus().equalsIgnoreCase(BookingsStatus.CHECKIN.name())) {
                noOfCheckedInTenants++;
            } else if (booking.getCurrentStatus().equalsIgnoreCase(BookingsStatus.NOTICE.name())) {
                noOfNoticeTenants++;
            } else if (booking.getCurrentStatus().equalsIgnoreCase(BookingsStatus.VACATED.name())) {
                noOfVacatedTenants++;
            } else if (booking.getCurrentStatus().equalsIgnoreCase(BookingsStatus.TERMINATED.name())) {
                noOfTerminatedTenants++;
            }
        }

        List<CustomerResponse> customerResponses = new ArrayList<>();

        if (agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Tenants.getId(), Utils.PERMISSION_READ)) {

            Set<String> customerIds = bookings.stream()
                    .map(BookingsV1::getCustomerId)
                    .collect(Collectors.toSet());

            List<Customers> customers = customersService.getCustomersByIds(customerIds);

            customerResponses = customers.stream()
                    .map(customer -> new CustomerResMapper().apply(customer))
                    .toList();
        }

        int noOfActiveTenants = noOfBookedTenants + noOfCheckedInTenants;

        List<Subscription> subscriptions = new ArrayList<>();

        if (agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Subscriptions.getId(), Utils.PERMISSION_READ)) {
            subscriptions = subscriptionService.getSubscriptionsByHostelId(hostelId);
        }

        List<UserActivities> activities = userActivitiesService.getActivitiesByHostelId(hostelId);

        Map<String, Users> userLookup = new HashMap<>();
        userLookup.put(owner.getUserId(), owner);
        masters.forEach(master -> userLookup.put(master.getUserId(), master));
        staffs.forEach(staff -> userLookup.put(staff.getUserId(), staff));

        List<AmenitiesV1> amenities = amenitiesService.getAmenitiesByHostelId(hostelId);

        HostelResponse hostelDetails = new HostelDetailsMapper(
                ownerInfo, noOfFloors, noOfRooms, noOfBeds, noOfActiveTenants, noOfBookedTenants,
                noOfCheckedInTenants, noOfNoticeTenants, noOfVacatedTenants, noOfTerminatedTenants,
                sharingTypeList, amenities, customerResponses, subscriptions, mastersRes, staffsRes,
                activities, userLookup
        ).apply(hostel);

        return new ResponseEntity<>(hostelDetails, HttpStatus.OK);
    }
}
