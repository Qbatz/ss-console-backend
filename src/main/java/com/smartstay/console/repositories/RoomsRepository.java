package com.smartstay.console.repositories;

import com.smartstay.console.dao.Rooms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomsRepository extends JpaRepository<Rooms, Integer> {
    List<Rooms> findAllByHostelIdAndIsActiveTrueAndIsDeletedFalse(String hostelId);
}
