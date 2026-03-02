package com.smartstay.console.repositories;

import com.smartstay.console.dao.CustomerDocuments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerDocumentRepository extends JpaRepository<CustomerDocuments, Long> {
    @Query("""
            SELECT cd FROM CustomerDocuments cd WHERE cd.hostelId=:hostelId AND cd.customerId IN (:customerIds)
            """)
    List<CustomerDocuments> findByHostelIdAndCustomerIds(String hostelId, List<String> customerIds);
}
