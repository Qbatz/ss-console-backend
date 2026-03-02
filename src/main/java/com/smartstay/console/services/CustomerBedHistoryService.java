package com.smartstay.console.services;

import com.smartstay.console.dao.CustomersBedHistory;
import com.smartstay.console.repositories.CustomerBedHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerBedHistoryService {
    @Autowired
    private CustomerBedHistoryRepository customerBedHistoryRepository;

    public List<CustomersBedHistory> findByCustomerIds(String hostelId, List<String> customerIds) {
        return customerBedHistoryRepository.findByHostelIdAndCustomerIds(hostelId, customerIds);
    }

    public void deleteAll(List<CustomersBedHistory> listCustomerBedHistory) {
        customerBedHistoryRepository.deleteAll(listCustomerBedHistory);
    }
}
