package com.smartstay.console.services;

import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.*;
import com.smartstay.console.dto.bed.BedSnapshot;
import com.smartstay.console.ennum.*;
import com.smartstay.console.repositories.BedsRepository;
import com.smartstay.console.responses.beds.*;
import com.smartstay.console.responses.customers.CustomerRes;
import com.smartstay.console.responses.date.DateFilterRes;
import com.smartstay.console.responses.hostels.HostelInfoRes;
import com.smartstay.console.responses.users.UserActivitiesResponse;
import com.smartstay.console.utils.SnapshotUtility;
import com.smartstay.console.utils.UserActivityUtil;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BedsService {

    @Autowired
    private BedsRepository bedsRepository;
    @Autowired
    private Authentication authentication;
    @Autowired
    private AgentService agentService;
    @Autowired
    private AgentRolesService agentRolesService;
    @Autowired
    private BookingsService bookingsService;
    @Autowired
    private AgentActivitiesService agentActivitiesService;
    @Autowired
    @Lazy
    private HostelsService hostelsService;
    @Autowired
    private FloorsService floorsService;
    @Autowired
    private RoomsService roomsService;
    @Autowired
    private UserActivitiesService userActivitiesService;
    @Autowired
    private HostelService hostelService;
    @Autowired
    @Lazy
    private UserActivityUtil userActivityUtil;
    @Autowired
    @Lazy
    private CustomersService customersService;

    public List<Beds> getBedsByHostelId(String hostelId) {
        return bedsRepository.findAllByHostelIdAndIsActiveTrueAndIsDeletedFalse(hostelId);
    }

    public List<Beds> findOccupiedBeds(String hostelId) {
        return bedsRepository
                .findAllByHostelIdAndCurrentStatusAndIsActiveTrueAndIsDeletedFalse(hostelId, BedStatus.OCCUPIED.name());
    }

    public void makeAllBedAvailable(List<Beds> listBeds) {
        listBeds.forEach(i -> {
                    i.setBooked(false);
                    i.setCurrentStatus(BedStatus.VACANT.name());
                    i.setFreeFrom(null);
                    i.setStatus(BedStatus.VACANT.name());
                });
        bedsRepository.saveAll(listBeds);
    }

    public List<Beds> getBedsByBedIds(Set<Integer> occupiedBedIds) {
        return bedsRepository.findAllByBedIdIn(occupiedBedIds);
    }

    public Beds getBedById(int bedId) {
        return bedsRepository.findByBedIdAndIsActiveTrueAndIsDeletedFalse(bedId);
    }

    public void deleteAll(List<Beds> listBeds) {
        bedsRepository.deleteAll(listBeds);
    }

    public void saveAll(List<Beds> bedsList) {
        bedsRepository.saveAll(bedsList);
    }

    public void save(Beds bed) {
        bedsRepository.save(bed);
    }

    public ResponseEntity<?> updateBedCurrentStatus(int bedId) {

        Agent loggedInAgent = agentService.findUserByUserId(authentication.getName());
        if (loggedInAgent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(loggedInAgent.getRoleId(),
                ModuleId.Hostels.getId(), Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        Beds bed = bedsRepository.findByBedIdAndIsActiveTrueAndIsDeletedFalse(bedId);
        if (bed == null) {
            return new ResponseEntity<>(Utils.BED_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        BedSnapshot oldSnapshot = SnapshotUtility.toSnapshot(bed);

        if (BedStatus.OCCUPIED.name().equals(bed.getCurrentStatus())) {
            return new ResponseEntity<>("Bed status is occupied already", HttpStatus.BAD_REQUEST);
        }

        BookingsV1 latestBooking = bookingsService.getLatestActiveBookingByBedId(bedId);
        if (latestBooking == null) {
            return new ResponseEntity<>("No active booking exists for this bed", HttpStatus.BAD_REQUEST);
        }

        Date today = new Date();

        bed.setCurrentStatus(BedStatus.OCCUPIED.name());
        bed.setUpdatedAt(today);

        bed = bedsRepository.save(bed);

        BedSnapshot newSnapshot = SnapshotUtility.toSnapshot(bed);

        agentActivitiesService.createAgentActivity(loggedInAgent, ActivityType.UPDATE, Source.BED,
                String.valueOf(bedId), oldSnapshot, newSnapshot);

        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);
    }

    public ResponseEntity<?> getHostelsWithBedInfo(int page, int size, String name) {

        Agent loggedInAgent = agentService.findUserByUserId(authentication.getName());
        if (loggedInAgent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(loggedInAgent.getRoleId(),
                ModuleId.Hostels.getId(), Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        name = (name == null || name.isBlank()) ? null : name.trim();

        page = Math.max(page - 1, 0);
        size = Math.max(size, 1);

        Page<HostelV1> pagedHostels = hostelsService
                .getAllPagedHostels(page, size, name);

        List<HostelV1> hostels = pagedHostels.getContent();

        Set<String> hostelIds = hostels.stream()
                .map(HostelV1::getHostelId)
                .collect(Collectors.toSet());

        List<Floors> floors = floorsService
                .getAllByHostelIds(hostelIds);

        Map<String, List<Floors>> floorsMap = floors.stream()
                .collect(Collectors.groupingBy(Floors::getHostelId));

        List<Rooms> rooms = roomsService
                .getAllByHostelIds(hostelIds);

        Map<String, List<Rooms>> roomsMap = rooms.stream()
                .collect(Collectors.groupingBy(Rooms::getHostelId));

        List<Beds> beds = bedsRepository
                .findAllByHostelIdInAndIsActiveTrueAndIsDeletedFalse(hostelIds);

        Map<String, List<Beds>> bedsMap = beds.stream()
                .collect(Collectors.groupingBy(Beds::getHostelId));

        List<HostelsBedInfoRes> hostelsRes = new ArrayList<>();

        for (HostelV1 hostel : hostels) {

            List<Floors> hostelFloors = floorsMap.getOrDefault(hostel.getHostelId(), new ArrayList<>());
            List<Rooms> hostelRooms = roomsMap.getOrDefault(hostel.getHostelId(), new ArrayList<>());
            List<Beds> hostelBeds = bedsMap.getOrDefault(hostel.getHostelId(), new ArrayList<>());

            List<FloorsResponse> hostelFloorsRes = new ArrayList<>();
            for (Floors floor : hostelFloors) {

                List<RoomsResponse> hostelRoomsRes = new ArrayList<>();
                for (Rooms room : hostelRooms) {

                    if (!floor.getFloorId().equals(room.getFloorId())) {
                        continue;
                    }

                    List<BedsResponse> hostelBedsRes = new ArrayList<>();
                    for (Beds bed : hostelBeds) {

                        if (!room.getRoomId().equals(bed.getRoomId())) {
                            continue;
                        }

                        hostelBedsRes.add(new BedsResponse(bed.getBedId(), bed.getBedName(), bed.getCurrentStatus()));
                    }

                    hostelRoomsRes.add(new RoomsResponse(room.getRoomId(), room.getRoomName(), hostelBedsRes));
                }

                hostelFloorsRes.add(new FloorsResponse(floor.getFloorId(), floor.getFloorName(), hostelRoomsRes));
            }

            hostelsRes.add(new HostelsBedInfoRes(hostel.getHostelId(), hostel.getHostelName(), hostel.getMainImage(),
                    Utils.getInitials(hostel.getHostelName()), hostel.getMobile(), hostel.getEmailId(),
                    Utils.buildFullAddress(hostel), hostelFloorsRes));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("hostels", hostelsRes);
        response.put("currentPage", page + 1);
        response.put("pageSize", size);
        response.put("totalItems", pagedHostels.getTotalElements());
        response.put("totalPages", pagedHostels.getTotalPages());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public long getBedCount() {
        return bedsRepository.findBedCount();
    }

    public ResponseEntity<?> getBedDetailsByBedId(int bedId, String dateFilter, Date startDate, Date endDate) {

        Agent loggedInAgent = agentService.findUserByUserId(authentication.getName());
        if (loggedInAgent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(loggedInAgent.getRoleId(),
                ModuleId.Hostels.getId(), Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        Beds bed = bedsRepository.findByBedIdAndIsActiveTrueAndIsDeletedFalse(bedId);
        if (bed == null) {
            return new ResponseEntity<>(Utils.BED_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        BookingsV1 latestBooking = bookingsService.getLatestActiveBookingByBedId(bedId);
        String customerId = null;
        if (latestBooking != null) {
            customerId = latestBooking.getCustomerId();
        }

        Customers customer = null;
        if (customerId != null) {
            customer = customersService.getCustomerInformation(customerId);
        }

        String hostelId = bed.getHostelId();
        HostelV1 hostel = hostelService.getHostelByHostelId(hostelId);

        String freeFromDate = null;
        String roomName = null;
        Integer floorId = null;
        String floorName = null;

        Rooms room = roomsService.getRoomById(bed.getRoomId());
        if (room != null) {
            roomName = room.getRoomName();
            floorId = room.getFloorId();

            Floors floor = floorsService.getByFloorId(floorId);
            if (floor != null) {
                floorName = floor.getFloorName();
            }
        }

        if (bed.getFreeFrom() != null) {
            freeFromDate = Utils.dateToString(bed.getFreeFrom());
        }

        DateFilterEnum2 dateFilterEnum;
        try {
            dateFilterEnum = DateFilterEnum2.valueOf(dateFilter);
        } catch (Exception e) {
            return new ResponseEntity<>(Utils.DATE_FILTER_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        Date today = new Date();

        if (DateFilterEnum2.CUSTOM.equals(dateFilterEnum)) {
            if (startDate == null || endDate == null){
                return new ResponseEntity<>(Utils.DATE_IS_NULL, HttpStatus.BAD_REQUEST);
            }
        } else if (DateFilterEnum2.THIS_MONTH.equals(dateFilterEnum)) {
            startDate = Utils.getStartOfMonth(today);
            endDate = Utils.getEndOfMonth(today);
        }  else if (DateFilterEnum2.LAST_MONTH.equals(dateFilterEnum)) {
            Date previousMonthDate = Utils.getPreviousMonthDate(today);
            startDate = Utils.getStartOfMonth(previousMonthDate);
            endDate = Utils.getEndOfMonth(previousMonthDate);
        }  else if (DateFilterEnum2.LAST_6_MONTHS.equals(dateFilterEnum)) {
            startDate = Utils.getStartOfLastSixMonths(today);
            endDate = Utils.getEndOfLastSixMonths(today);
        }

        startDate = Utils.getStartOfDay(startDate);
        endDate = Utils.getEndOfDay(Utils.addDaysToDate(endDate, 1));

        List<UserActivities> bedActivities = userActivitiesService
                .getActivitiesByHostelIdAndSourceAndSourceIdAndBetweenDate(hostelId, ActivitySource.BEDS.name(),
                        String.valueOf(bedId), startDate, endDate);

        List<UserActivitiesResponse> bedActivitiesRes = userActivityUtil
                .buildResponses(bedActivities);

        HostelInfoRes hostelRes = null;
        if (hostel != null) {
            hostelRes = new HostelInfoRes(hostel.getHostelId(), hostel.getHostelName(),
                    Utils.getInitials(hostel.getHostelName()), hostel.getMobile(), hostel.getEmailId(),
                    hostel.getHouseNo(), hostel.getStreet(), hostel.getLandmark(), hostel.getCity(),
                    hostel.getState(), hostel.getCountry(), hostel.getPincode(), Utils.buildFullAddress(hostel),
                    hostel.getMainImage());
        }

        CustomerRes customerRes = null;
        if (customer != null) {

            String joiningDate = null;
            if (customer.getJoiningDate() != null) {
                joiningDate = Utils.dateToString(customer.getJoiningDate());
            }

            customerRes = new CustomerRes(customer.getCustomerId(), customer.getFirstName(),
                    customer.getLastName(), Utils.getFullName(customer.getFirstName(), customer.getLastName()),
                    Utils.getInitials(customer.getFirstName(), customer.getLastName()), Utils.maskMobileNo(customer.getMobile()),
                    customer.getEmailId(), customer.getCurrentStatus(), joiningDate);
        }


        BedsInfoResponse bedInfoRes = new BedsInfoResponse(bedId, bed.getBedName(), bed.getRoomId(), roomName,
                floorId, floorName, bed.isBooked(), bed.getRentAmount(), bed.getCurrentStatus(), freeFromDate,
                customerRes, hostelRes, bedActivitiesRes);

        List<DateFilterRes> dateFilterRes = Arrays.stream(DateFilterEnum2.values())
                .map(i -> new DateFilterRes(i.name(), i.getLabel()))
                .toList();

        BedsInfoResWrapper resWrapper = new BedsInfoResWrapper(dateFilterRes, bedInfoRes);

        return new ResponseEntity<>(resWrapper, HttpStatus.OK);
    }
}
