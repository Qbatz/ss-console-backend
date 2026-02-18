package com.smartstay.console.services;

import com.smartstay.console.repositories.FloorsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FloorsService {

    @Autowired
    FloorsRepository floorsRepository;

    public int getCountByHostelId(String hostelId){
        return floorsRepository.countByHostelId(hostelId);
    }
}
