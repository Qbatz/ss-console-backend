package com.smartstay.console.services;

import com.smartstay.console.dao.Beds;
import com.smartstay.console.ennum.BedStatus;
import com.smartstay.console.repositories.BedsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class BedsService {

    @Autowired
    BedsRepository bedsRepository;

    public List<Beds> getBedsByHostelId(String hostelId) {
        return bedsRepository.findAllByHostelIdAndIsActiveTrueAndIsDeletedFalse(hostelId);
    }

    public List<Beds> findOccupiedBeds(String hostelId) {
        return bedsRepository.findAllByHostelIdAndIsActiveTrueAndIsDeletedFalse(hostelId);
    }

    public void makeAllBedAvailabe(List<Beds> listBeds) {
        List<Beds> beds = listBeds
                .stream()
                .map(i -> {
                    i.setBooked(false);
                    i.setCurrentStatus(BedStatus.VACANT.name());
                    i.setFreeFrom(null);
                    i.setStatus(BedStatus.VACANT.name());
                    return i;
                })
                .toList();
        bedsRepository.saveAll(beds);
    }

    public List<Beds> getBedsByBedIds(Set<Integer> occupiedBedIds) {
        return bedsRepository.findAllByBedIdIn(occupiedBedIds);
    }

    public void deleteAll(List<Beds> listBeds) {
        bedsRepository.deleteAll(listBeds);
    }
}
