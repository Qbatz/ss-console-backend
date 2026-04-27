package com.smartstay.console.repositories;

import com.smartstay.console.dao.CustomerBillingRules;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerBillingRulesRepository extends JpaRepository<CustomerBillingRules, String> {

    List<CustomerBillingRules> findAllByHostelIdAndCustomerId(String hostelId, String customerId);

    List<CustomerBillingRules> findAllByHostelIdAndCustomerIdIn(String hostelId, List<String> customerIds);
}
