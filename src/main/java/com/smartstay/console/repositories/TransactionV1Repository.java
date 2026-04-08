package com.smartstay.console.repositories;

import com.smartstay.console.dao.TransactionV1;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionV1Repository extends JpaRepository<TransactionV1, String> {

    List<TransactionV1> findByHostelIdAndCustomerIdIn(String hostelId, List<String> customerIds);

    List<TransactionV1> findByHostelIdAndCustomerId(String hostelId, String customerId);
}
