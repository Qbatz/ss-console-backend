package com.smartstay.console.services;

import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.Agent;
import com.smartstay.console.dao.SettlementDetails;
import com.smartstay.console.dto.settlementDetails.SettlementDetailsSnapshot;
import com.smartstay.console.ennum.ActivityType;
import com.smartstay.console.ennum.Source;
import com.smartstay.console.repositories.SettlementDetailsRepository;
import com.smartstay.console.utils.SnapshotUtility;
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

    public SettlementDetails findByCustomerId(String customerId) {
        return settlementDetailsRepository.findByCustomerId(customerId);
    }

    public void deleteAll(List<SettlementDetails> listSettlementDetails) {
        settlementDetailsRepository.deleteAll(listSettlementDetails);
    }

    public void delete(SettlementDetails settlementDetails) {
        settlementDetailsRepository.delete(settlementDetails);
    }

    public List<SettlementDetails> findByCustomerIds(List<String> customerIds) {
        return settlementDetailsRepository.findAllByCustomerIdIn(customerIds);
    }

    public SettlementDetails addSettlementForCustomer(String customerId, Date leavingDate,
                                                      Agent loggedInAgent) {

        SettlementDetails settlementDetails = settlementDetailsRepository
                .findByCustomerId(customerId);

        Date today = new Date();
        boolean isCreate = false;

        if (settlementDetails == null) {
            isCreate = true;

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

        if (isCreate){
            SettlementDetailsSnapshot snapshot = SnapshotUtility.toSnapshot(settlementDetails);

            agentActivitiesService.createAgentActivity(loggedInAgent, ActivityType.CREATE, Source.SETTLEMENT_DETAILS,
                    String.valueOf(settlementDetails.getId()), null, snapshot);
        }

        return settlementDetails;
    }
}
