package com.smartstay.console.services;

import com.smartstay.console.dao.Rooms;
import com.smartstay.console.repositories.RoomsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class RoomsService {

    @Autowired
    private RoomsRepository roomsRepository;

    public List<Rooms> getRoomsByHostelId(String hostelId) {
        return roomsRepository.findAllByHostelIdAndIsActiveTrueAndIsDeletedFalse(hostelId);
    }

    public void deleteAll(List<Rooms> listRooms) {
        roomsRepository.deleteAll(listRooms);
    }

    public Rooms getRoomById(int roomId) {
        return roomsRepository.findByRoomIdAndIsActiveTrueAndIsDeletedFalse(roomId);
    }

    public List<Rooms> getRoomsByRoomIds(Set<Integer> roomIds) {
        return roomsRepository.findAllByRoomIdInAndIsActiveTrueAndIsDeletedFalse(roomIds);
    }
}
