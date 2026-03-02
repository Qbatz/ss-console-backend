package com.smartstay.console.services;

import com.smartstay.console.dao.HostelReadings;
import com.smartstay.console.repositories.HostelReadingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HostelReadingService {
    @Autowired
    private HostelReadingRepository hostelReadingRepository;

    public List<HostelReadings> findByHostelId(String hostelId) {
        return hostelReadingRepository.findByHostelId(hostelId);
    }

    public void deleteAll(List<HostelReadings> listHostelReadings) {
        hostelReadingRepository.deleteAll(listHostelReadings);
    }
}
