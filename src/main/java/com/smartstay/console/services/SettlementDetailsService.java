package com.smartstay.console.services;

import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.SettlementDetails;
import com.smartstay.console.ennum.ActivityType;
import com.smartstay.console.ennum.Source;
import com.smartstay.console.repositories.SettlementDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class SettlementDetailsService {

    @Autowired
    private SettlementDetailsRepository settlementDetailsRepository;
    @Autowired
    private Authentication authentication;
    @Autowired
    private AgentActivitiesService agentActivitiesService;

    public List<SettlementDetails> findByCustomerId(String customerId) {
        return settlementDetailsRepository.findAllByCustomerId(customerId);
    }

    public void deleteAll(List<SettlementDetails> listSettlementDetails) {
        settlementDetailsRepository.deleteAll(listSettlementDetails);
    }

    public List<SettlementDetails> findByCustomerIds(List<String> customerIds) {
        return settlementDetailsRepository.findAllByCustomerIdIn(customerIds);
    }

    public SettlementDetails addSettlementForCustomer(String customerId, Date leavingDate) {

        SettlementDetails settlementDetails = settlementDetailsRepository
                .findByCustomerId(customerId);

        Date today = new Date();

        if (settlementDetails == null) {
            settlementDetails = new SettlementDetails();
            settlementDetails.setCustomerId(customerId);
            settlementDetails.setLeavingDate(leavingDate);
            settlementDetails.setCreatedAt(today);
            settlementDetails.setCreatedBy(authentication.getName());
        }
        else {
            settlementDetails.setLeavingDate(leavingDate);
            settlementDetails.setUpdatedAt(today);
            settlementDetails.setUpdatedBy(authentication.getName());
        }

        settlementDetails = settlementDetailsRepository.save(settlementDetails);

        return settlementDetails;
    }
}
