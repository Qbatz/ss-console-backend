package com.smartstay.console.repositories;

import com.smartstay.console.dao.CustomerBillingRules;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerBillingRulesRepository extends JpaRepository<CustomerBillingRules, String> {
}
