package com.smartstay.console.repositories;

import com.smartstay.console.dao.CustomerWalletHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface CustomerWalletHistoryRepository extends JpaRepository<CustomerWalletHistory, Long> {

    List<CustomerWalletHistory> findAllByInvoiceIdIn(Set<String> invoiceIds);
}
