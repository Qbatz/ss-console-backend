package com.smartstay.console.services;

import com.smartstay.console.repositories.BedsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BedsService {

    @Autowired
    BedsRepository bedsRepository;

    public int getCountByHostelId(String hostelId){
        return bedsRepository.countByHostelId(hostelId);
    }
}
