package com.smartstay.console.repositories;

import com.smartstay.console.dao.InvoiceDiscounts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceDiscountsRepositories extends JpaRepository<InvoiceDiscounts, Long> {
}
