package com.smartstay.console.services;

import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.Agent;
import com.smartstay.console.dao.Beds;
import com.smartstay.console.dao.BookingsV1;
import com.smartstay.console.dto.bed.BedSnapshot;
import com.smartstay.console.ennum.ActivityType;
import com.smartstay.console.ennum.BedStatus;
import com.smartstay.console.ennum.ModuleId;
import com.smartstay.console.ennum.Source;
import com.smartstay.console.repositories.BedsRepository;
import com.smartstay.console.utils.SnapshotUtility;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;

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
                ModuleId.Tenants.getId(), Utils.PERMISSION_READ)) {
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
}
