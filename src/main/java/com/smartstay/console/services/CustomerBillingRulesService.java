package com.smartstay.console.services;

import com.smartstay.console.dao.CustomerBillingRules;
import com.smartstay.console.repositories.CustomerBillingRulesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerBillingRulesService {

    @Autowired
    private CustomerBillingRulesRepository customerBillingRulesRepository;

    public List<CustomerBillingRules> findByHostelIdAndCustomerId(String hostelId, String customerId) {
        return customerBillingRulesRepository.findAllByHostelIdAndCustomerId(hostelId, customerId);
    }

    public void deleteAll(List<CustomerBillingRules> listCustomerBillingRules) {
        customerBillingRulesRepository.deleteAll(listCustomerBillingRules);
    }

    public List<CustomerBillingRules> findByHostelIdAndCustomerIds(String hostelId, List<String> customerIds) {
        return customerBillingRulesRepository.findAllByHostelIdAndCustomerIdIn(hostelId, customerIds);
    }
}
