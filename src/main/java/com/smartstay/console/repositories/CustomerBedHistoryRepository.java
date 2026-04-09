package com.smartstay.console.repositories;

import com.smartstay.console.dao.CustomersBedHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface CustomerBedHistoryRepository extends JpaRepository<CustomersBedHistory, Long> {

    @Query("""
            SELECT cbh FROM CustomersBedHistory cbh WHERE cbh.hostelId=:hostelId and cbh.customerId IN (:customerId)
            """)
    List<CustomersBedHistory> findByHostelIdAndCustomerIds(String hostelId, List<String> customerId);

    List<CustomersBedHistory> findByHostelIdAndCustomerId(String hostelId, String customerId);

    @Query(value = """
        SELECT * FROM customers_bed_history cbh WHERE cbh.customer_id IN (:customerId) AND 
        DATE(cbh.start_date) <=DATE(:endDate) AND (cbh.end_date IS NULL OR DATE(cbh.end_date) >= DATE(:startDate))
        """, nativeQuery = true)
    List<CustomersBedHistory> findByListCustomerIdsAndStartAndEndDate(@Param("customerId") List<String> customerId,
                                                                      @Param("startDate") Date startDate,
                                                                      @Param("endDate") Date endDate);
}
