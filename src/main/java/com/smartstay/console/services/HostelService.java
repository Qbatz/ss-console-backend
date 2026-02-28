package com.smartstay.console.services;

import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.HostelV1;
import com.smartstay.console.repositories.HostelV1Repositories;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class HostelService {
    @Autowired
    private Authentication authentication;
    @Autowired
    private HostelV1Repositories hostelRepository;

    public HostelV1 getHostelInfo(String hostelId) {
        if (!authentication.isAuthenticated()) {
            return null;
        }
        try {
            HostelV1 hostelV1 = hostelRepository.getReferenceById(hostelId);
            return hostelV1;
        }
        catch (EntityNotFoundException ene) {
            return null;
        }

    }

    public void updateHostel(HostelV1 hostelV1) {
        hostelRepository.save(hostelV1);
    }

    public List<HostelV1> getHostelsByHostelIds(Set<String> hostelIds) {
        return hostelRepository.findAllByHostelIdIn(hostelIds);
    }
}
