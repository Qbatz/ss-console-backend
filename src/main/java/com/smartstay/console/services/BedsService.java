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
    private BedsRepository bedsRepository;

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
}
