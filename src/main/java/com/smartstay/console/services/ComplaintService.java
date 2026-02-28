package com.smartstay.console.services;

import com.smartstay.console.dao.ComplaintsV1;
import com.smartstay.console.repositories.ComplaintsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ComplaintService {
    @Autowired
    private ComplaintsRepository complaintsRepository;


    public List<ComplaintsV1> findByHostelIdAndCustomerIdIn(String hostelId, List<String> customerIds) {
        return complaintsRepository.findByHostelIdAndCustomerIdIn(hostelId, customerIds);
    }
}
