package com.smartstay.console.services;

import com.smartstay.console.dao.CustomersConfig;
import com.smartstay.console.repositories.CustomersConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerConfigService {

    @Autowired
    private CustomersConfigRepository customersConfigRepository;

    public List<CustomersConfig> findByHostelIdAndCustomerIds(String hostelId,
                                                              List<String> customerIds) {
        return customersConfigRepository.findByHostelIdAndCustomerIds(hostelId, customerIds);
    }

    public void deleteAll(List<CustomersConfig> listConfigs) {
        customersConfigRepository.deleteAll(listConfigs);
    }

    public List<CustomersConfig> findByHostelIdAndCustomerId(String hostelId, String customerId) {
        return customersConfigRepository.findByHostelIdAndCustomerId(hostelId, customerId);
    }

    public List<CustomersConfig> getAllActiveAndEnabledRecurringCustomers(String hostelId) {
        List<CustomersConfig> listCustomers = customersConfigRepository
                .findActiveAndRecurringEnabledCustomersByHostelId(hostelId);
        if (listCustomers == null) {
            return new ArrayList<>();
        }
        return listCustomers;
    }
}
