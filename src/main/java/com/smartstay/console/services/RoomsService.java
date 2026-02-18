package com.smartstay.console.services;

import com.smartstay.console.repositories.RoomsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoomsService {

    @Autowired
    RoomsRepository roomsRepository;

    public int getCountByHostelId(String hostelId){
        return roomsRepository.countByHostelId(hostelId);
    }
}
