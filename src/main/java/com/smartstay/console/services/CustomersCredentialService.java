package com.smartstay.console.services;

import com.smartstay.console.dao.CustomerCredentials;
import com.smartstay.console.repositories.CustomerCredentialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomersCredentialService {
    @Autowired
    private CustomerCredentialRepository customerCredentialRepository;

    public List<CustomerCredentials> findByHostelIdAndCustomerIds(String hostelId, List<String> customerIds) {
        return null;
    }
}
