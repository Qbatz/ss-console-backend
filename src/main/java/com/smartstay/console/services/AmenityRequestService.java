package com.smartstay.console.services;

import com.smartstay.console.dao.AmenityRequest;
import com.smartstay.console.repositories.AmenityRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AmenityRequestService {
    @Autowired
    private AmenityRequestRepository amenityRequestRepository;

    public List<AmenityRequest> findByHostelIdAndCustomerIds(String hostelId, List<String> customerIds) {
        return amenityRequestRepository.findByHostelIdAndCustomerIdIn(hostelId, customerIds);
    }
}
