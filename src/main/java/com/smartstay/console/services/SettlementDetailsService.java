package com.smartstay.console.services;

import com.smartstay.console.dao.SettlementDetails;
import com.smartstay.console.repositories.SettlementDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SettlementDetailsService {

    @Autowired
    private SettlementDetailsRepository settlementDetailsRepository;

    public List<SettlementDetails> findByCustomerId(String customerId) {
        return settlementDetailsRepository.findAllByCustomerId(customerId);
    }

    public void deleteAll(List<SettlementDetails> listSettlementDetails) {
        settlementDetailsRepository.deleteAll(listSettlementDetails);
    }

    public List<SettlementDetails> findByCustomerIds(List<String> customerIds) {
        return settlementDetailsRepository.findAllByCustomerIdIn(customerIds);
    }
}
