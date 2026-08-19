package com.smartstay.console.services;

import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.*;
import com.smartstay.console.dto.bed.BedSnapshot;
import com.smartstay.console.ennum.ActivityType;
import com.smartstay.console.ennum.BedStatus;
import com.smartstay.console.ennum.ModuleId;
import com.smartstay.console.ennum.Source;
import com.smartstay.console.repositories.BedsRepository;
import com.smartstay.console.responses.beds.BedsResponse;
import com.smartstay.console.responses.beds.FloorsResponse;
import com.smartstay.console.responses.beds.HostelsBedInfoRes;
import com.smartstay.console.responses.beds.RoomsResponse;
import com.smartstay.console.utils.SnapshotUtility;
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
}
