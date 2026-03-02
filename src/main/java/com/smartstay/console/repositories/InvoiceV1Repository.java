package com.smartstay.console.repositories;

import com.smartstay.console.dao.InvoicesV1;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceV1Repository extends JpaRepository<InvoicesV1, String> {
    @Query("""
            SELECT i FROM invoicesv1 i WHERE i.hostelId=:hostelId AND i.customerId IN (:customerIds)
            """)
    List<InvoicesV1> findByHostelIdAndCustomerIdIn(String hostelId, List<String> customerIds);
}
