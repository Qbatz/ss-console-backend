package com.smartstay.console.repositories;

import com.smartstay.console.dao.TenantBankTransactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TenantBankTransactionsRepository extends JpaRepository<TenantBankTransactions, Long> {

    List<TenantBankTransactions> findAllByCustomerIdIn(List<String> customerIds);

    List<TenantBankTransactions> findAllByCustomerId(String customerId);
}
