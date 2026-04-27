package com.smartstay.console.services;

import com.smartstay.console.dao.CustomersEbHistory;
import com.smartstay.console.repositories.CustomerEbHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerEbHistoryService {

    @Autowired
    private CustomerEbHistoryRepository customerEbHistoryRepository;

    public List<CustomersEbHistory> findByCustomerIds(List<String> customerIds) {
        return customerEbHistoryRepository.findByCustomerIdIn(customerIds);
    }

    public void deleteAll(List<CustomersEbHistory> listCustomerEbHistory) {
        customerEbHistoryRepository.deleteAll(listCustomerEbHistory);
    }

    public List<CustomersEbHistory> findByCustomerId(String customerId) {
        return customerEbHistoryRepository.findByCustomerId(customerId);
    }

    public List<CustomersEbHistory> getAllByCustomerIdAndReadingId(String customerId, List<Integer> ebReadingsId) {
        return customerEbHistoryRepository.findByCustomerIdAndReadingsId(customerId, ebReadingsId);
    }
}
