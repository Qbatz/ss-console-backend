package com.smartstay.console.services;

import com.smartstay.console.dao.CustomersEbHistory;
import com.smartstay.console.repositories.CustomerEbHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerEbHistoryService {
    @Autowired
    private CustomerEbHistoryRepository customerEbHistoryRepository;

    public List<CustomersEbHistory> findByCustomerIdAndHostelId(String hostelId, List<String> customerIds) {
        return customerEbHistoryRepository.findByCustomerIdIn(customerIds);
    }

    public void deleteAll(List<CustomersEbHistory> listCustomerEbHistory) {
        customerEbHistoryRepository.deleteAll(listCustomerEbHistory);
    }
}
