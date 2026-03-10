package com.smartstay.console.services;

import com.smartstay.console.dao.HotelType;
import com.smartstay.console.repositories.HotelTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HotelTypeService {

    @Autowired
    HotelTypeRepository hotelTypeRepository;

    public List<HotelType> getAllHotelTypes() {
        return hotelTypeRepository.findAllByIsActiveTrue();
    }
}
