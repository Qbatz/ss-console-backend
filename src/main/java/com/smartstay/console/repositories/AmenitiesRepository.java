package com.smartstay.console.repositories;

import com.smartstay.console.dao.AmenitiesV1;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AmenitiesRepository extends JpaRepository<AmenitiesV1, String> {
    List<AmenitiesV1> findAllByHostelIdAndIsActiveTrueAndIsDeletedFalse(String hostelId);
}
