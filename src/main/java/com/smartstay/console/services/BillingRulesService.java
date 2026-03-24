package com.smartstay.console.services;

import com.smartstay.console.dao.BillingRules;
import com.smartstay.console.repositories.BillingRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Set;

@Service
public class BillingRulesService {

    @Autowired
    BillingRuleRepository billingRuleRepository;

    public Page<BillingRules> getPaginatedBillingRulesByDays(Set<Integer> days, String hostelName,
                                                             int currentMonth, int currentYear,
                                                             String status, Pageable pageable) {
        return billingRuleRepository.getPaginatedBillingRulesByDays(
                days, hostelName, currentMonth, currentYear, status, pageable);
    }

    public BillingRules getCurrentMonthTemplate(String hostelId) {
        return billingRuleRepository.findCurrentBillingRules(hostelId);
    }

    public long getPendingRecurringCount(Set<Integer> days, String hostelName, int currentMonth, int currentYear){
        return billingRuleRepository.countPendingRecurring(days, hostelName, currentMonth, currentYear);
    }

    public long getExpiredSubscriptionsCount(Set<Integer> days, String hostelName, Date date){
        return billingRuleRepository.countExpiredSubscriptions(days, hostelName, date);
    }
}
