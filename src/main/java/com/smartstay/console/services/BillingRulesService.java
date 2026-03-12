package com.smartstay.console.services;

import com.smartstay.console.dao.BillingRules;
import com.smartstay.console.repositories.BillingRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class BillingRulesService {

    @Autowired
    BillingRuleRepository billingRuleRepository;

    public Page<BillingRules> getPaginatedBillingRulesByDays(Set<Integer> days, String hostelName, Pageable pageable) {
        return billingRuleRepository.findAllHostelsHavingRecurringByDays(days, hostelName, pageable);
    }
}
