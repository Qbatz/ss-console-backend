package com.smartstay.console.services;

import com.smartstay.console.dao.RentHistory;
import com.smartstay.console.repositories.RentHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RentHistoryService {

    @Autowired
    private RentHistoryRepository rentHistoryRepository;

    public List<RentHistory> findByCustomerId(String customerId) {
        return rentHistoryRepository.findAllByCustomerId(customerId);
    }

    public void deleteAll(List<RentHistory> listRentHistory) {
        rentHistoryRepository.deleteAll(listRentHistory);
    }

    public List<RentHistory> findByCustomerIds(List<String> customerIds) {
        return rentHistoryRepository.findAllByCustomerIdIn(customerIds);
    }
}
