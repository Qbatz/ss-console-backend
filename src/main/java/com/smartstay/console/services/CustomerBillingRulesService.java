package com.smartstay.console.services;

import com.smartstay.console.repositories.CustomerBillingRulesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomerBillingRulesService {

    @Autowired
    private CustomerBillingRulesRepository customerBillingRulesRepository;
}
