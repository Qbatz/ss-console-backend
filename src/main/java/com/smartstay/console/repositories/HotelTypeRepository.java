package com.smartstay.console.repositories;

import com.smartstay.console.dao.HotelType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotelTypeRepository extends JpaRepository<HotelType, Integer> {

    List<HotelType> findAllByIsActiveTrue();
}
