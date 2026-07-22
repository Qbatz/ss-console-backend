package com.smartstay.console.repositories;

import com.smartstay.console.dao.CustomerJobDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerJobDetailsRepository extends JpaRepository<CustomerJobDetails, Long> {

    List<CustomerJobDetails> findAllByCustomerIdIn(List<String> customerIds);

    List<CustomerJobDetails> findAllByCustomerId(String customerId);
}
