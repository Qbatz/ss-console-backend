package com.smartstay.console.services;

import com.smartstay.console.dao.AmenitiesV1;
import com.smartstay.console.repositories.AmenitiesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AmenitiesService {

    @Autowired
    AmenitiesRepository amenitiesRepository;

    public List<AmenitiesV1> getAmenitiesByHostelId(String hostelId){
        return amenitiesRepository
                .findAllByHostelIdAndIsActiveTrueAndIsDeletedFalse(hostelId);
    }
}
