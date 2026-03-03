package com.smartstay.console.repositories;

import com.smartstay.console.dao.CustomersEbHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerEbHistoryRepository extends JpaRepository<CustomersEbHistory, Long> {
    List<CustomersEbHistory> findByCustomerIdIn(List<String> customerIds);

    List<CustomersEbHistory> findByCustomerId(String customerId);
}
