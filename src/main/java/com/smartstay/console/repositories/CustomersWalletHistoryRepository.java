package com.smartstay.console.repositories;

import com.smartstay.console.dao.CustomerWalletHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface CustomersWalletHistoryRepository extends JpaRepository<CustomerWalletHistory, Long> {

    List<CustomerWalletHistory> findByCustomerIdIn(List<String> customerIds);

    List<CustomerWalletHistory> findByCustomerId(String customerId);

    @Query("""
        SELECT cwh FROM CustomerWalletHistory cwh
        WHERE cwh.customerId=:customerId
            AND cwh.billingStatus='INVOICE_NOT_GENERATED'
        """)
    List<CustomerWalletHistory> findInvoiceNotGeneratedByCustomerId(String customerId);

    List<CustomerWalletHistory> findAllByInvoiceIdIn(Set<String> invoiceIds);
}
