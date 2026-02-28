package com.smartstay.console.services;

import com.smartstay.console.dao.Rooms;
import com.smartstay.console.repositories.RoomsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomsService {

    @Autowired
    RoomsRepository roomsRepository;

    public List<Rooms> getRoomsByHostelId(String hostelId) {
        return roomsRepository.findAllByHostelIdAndIsActiveTrueAndIsDeletedFalse(hostelId);
    }
}
