package com.smartstay.console.services;

import com.smartstay.console.dao.Floors;
import com.smartstay.console.repositories.FloorsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FloorsService {

    @Autowired
    FloorsRepository floorsRepository;

    public int getCountByHostelId(String hostelId){
        return floorsRepository.countByHostelIdAndIsActiveTrueAndIsDeletedFalse(hostelId);
    }

    public void deleteAll(List<Floors> listFloors) {
        floorsRepository.deleteAll(listFloors);
    }

    public List<Floors> findByHostelId(String hostelId) {
        return floorsRepository.findAllByHostelId(hostelId);
    }
}
