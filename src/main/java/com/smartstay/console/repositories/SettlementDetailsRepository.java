package com.smartstay.console.repositories;

import com.smartstay.console.dao.SettlementDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SettlementDetailsRepository extends JpaRepository<SettlementDetails, Long> {

    List<SettlementDetails> findAllByCustomerIdIn(List<String> customerIds);

    SettlementDetails findByCustomerId(String customerId);
}
