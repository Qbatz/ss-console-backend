package com.smartstay.console.repositories;

import com.smartstay.console.dao.CustomersBedHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerBedHistoryRepository extends JpaRepository<CustomersBedHistory, Long> {
    @Query("""
            SELECT cbh FROM CustomersBedHistory cbh WHERE cbh.hostelId=:hostelId and cbh.customerId IN (:customerId)
            """)
    List<CustomersBedHistory> findByHostelIdAndCustomerIds(String hostelId, List<String> customerId);

    List<CustomersBedHistory> findByHostelIdAndCustomerId(String hostelId, String customerId);
}
