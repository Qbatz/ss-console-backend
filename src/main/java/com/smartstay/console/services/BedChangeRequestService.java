package com.smartstay.console.services;

import com.smartstay.console.dao.BedChangeRequest;
import com.smartstay.console.repositories.BedChangeRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BedChangeRequestService {

    @Autowired
    private BedChangeRequestRepository bedChangeRequestRepository;

    public List<BedChangeRequest> findByHostelIdAndCustomerId(String hostelId, String customerId) {
        return bedChangeRequestRepository.findAllByHostelIdAndCustomerId(hostelId, customerId);
    }

    public void deleteAll(List<BedChangeRequest> listBedChangeRequests) {
        bedChangeRequestRepository.deleteAll(listBedChangeRequests);
    }

    public List<BedChangeRequest> findByHostelIdAndCustomerIds(String hostelId, List<String> customerIds) {
        return bedChangeRequestRepository.findAllByHostelIdAndCustomerIdIn(hostelId, customerIds);
    }
}
