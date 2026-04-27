package com.smartstay.console.repositories;

import com.smartstay.console.dao.InvoiceDiscounts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface InvoiceDiscountsRepository extends JpaRepository<InvoiceDiscounts, Long> {

    List<InvoiceDiscounts> findAllByInvoiceIdIn(Set<String> invoiceIds);

    List<InvoiceDiscounts> findAllByHostelIdAndCustomerId(String hostelId, String customerId);

    List<InvoiceDiscounts> findAllByHostelIdAndCustomerIdIn(String hostelId, List<String> customerIds);
}
