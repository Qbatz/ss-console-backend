package com.smartstay.console.services;

import com.smartstay.console.dao.CustomersConfig;
import com.smartstay.console.repositories.CustomersConfigReposotory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerConfigService {
    @Autowired
    private CustomersConfigReposotory customersConfigReposotory;
    public List<CustomersConfig> findByHostelIdAndCustomerIds(String hostelId, List<String> customerIds) {
        return customersConfigReposotory.findByHostelIdAndCustomerIds(hostelId, customerIds);
    }
}
