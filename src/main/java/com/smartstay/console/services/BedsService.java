package com.smartstay.console.services;

import com.smartstay.console.dao.Beds;
import com.smartstay.console.repositories.BedsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BedsService {

    @Autowired
    BedsRepository bedsRepository;

    public List<Beds> getBedsByHostelId(String hostelId) {
        return bedsRepository.findAllByHostelIdAndIsActiveTrueAndIsDeletedFalse(hostelId);
    }
}
