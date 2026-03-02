package com.smartstay.console.services;

import com.smartstay.console.dao.CustomersAmenity;
import com.smartstay.console.repositories.CustomersAmenityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomersAmenityService {
    @Autowired
    private CustomersAmenityRepository customersAmenityRepository;
    public List<CustomersAmenity> findByHostelIdAndCustomerIdIs(List<String> customerIds) {
        return customersAmenityRepository.findByCustomerIdIn(customerIds);
    }

    public void deleteAll(List<CustomersAmenity> listCustomersAmenity) {
        customersAmenityRepository.deleteAll(listCustomersAmenity);
    }
}
