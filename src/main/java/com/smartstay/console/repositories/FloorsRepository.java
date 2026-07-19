package com.smartstay.console.repositories;

import com.smartstay.console.dao.Floors;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface FloorsRepository extends JpaRepository<Floors, Integer> {

    int countByHostelIdAndIsActiveTrueAndIsDeletedFalse(String hostelId);

    List<Floors> findAllByHostelId(String hostelId);

    Floors findByFloorIdAndIsActiveTrueAndIsDeletedFalse(int floorId);

    List<Floors> findAllByFloorIdInAndIsActiveTrueAndIsDeletedFalse(Set<Integer> floorIds);
}
