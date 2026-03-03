package com.smartstay.console.repositories;

import com.smartstay.console.dao.CustomerWalletHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomersWalletRepository extends JpaRepository<CustomerWalletHistory, Long> {
    List<CustomerWalletHistory> findByCustomerIdIn(List<String> customerIds);

    List<CustomerWalletHistory> findByCustomerId(String customerId);
}
