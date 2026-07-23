package com.smartstay.console.services;

import com.smartstay.console.dao.CustomerJobDetails;
import com.smartstay.console.repositories.CustomerJobDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerJobDetailsService {

    @Autowired
    private CustomerJobDetailsRepository customerJobDetailsRepository;

    public List<CustomerJobDetails> getByCustomerIds(List<String> customerIds) {
        return customerJobDetailsRepository.findAllByCustomerIdIn(customerIds);
    }

    public void deleteAll(List<CustomerJobDetails> listCustomerJobDetails) {
        customerJobDetailsRepository.deleteAll(listCustomerJobDetails);
    }

    public List<CustomerJobDetails> getByCustomerId(String customerId) {
        return customerJobDetailsRepository.findAllByCustomerId(customerId);
    }
}
