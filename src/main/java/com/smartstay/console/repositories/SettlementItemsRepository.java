package com.smartstay.console.repositories;

import com.smartstay.console.dao.SettlementItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface SettlementItemsRepository extends JpaRepository<SettlementItems, Long> {

    List<SettlementItems> findAllByInvoiceIdIn(Set<String> invoiceIds);

    List<SettlementItems> findAllByCustomerId(String customerId);

    List<SettlementItems> findAllByCustomerIdIn(Set<String> customerIdsSet);

    SettlementItems findByInvoiceId(String invoiceId);
}
