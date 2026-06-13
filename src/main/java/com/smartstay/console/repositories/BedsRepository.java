package com.smartstay.console.repositories;

import com.smartstay.console.dao.Beds;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface BedsRepository extends JpaRepository<Beds, Integer> {

    List<Beds> findAllByHostelIdAndIsActiveTrueAndIsDeletedFalse(String hostelId);

    List<Beds> findAllByHostelIdAndStatusAndIsActiveTrueAndIsDeletedFalse(String hostelId, String status);

    List<Beds> findAllByBedIdIn(Set<Integer> occupiedBedIds);
}
