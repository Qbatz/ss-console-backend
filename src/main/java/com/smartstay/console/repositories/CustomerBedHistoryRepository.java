package com.smartstay.console.repositories;

import com.smartstay.console.dao.CustomersBedHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Repository
public interface CustomerBedHistoryRepository extends JpaRepository<CustomersBedHistory, Long> {

    @Query("""
            SELECT cbh FROM CustomersBedHistory cbh
            WHERE cbh.hostelId = :hostelId
                and cbh.customerId IN (:customerId)
            """)
    List<CustomersBedHistory> findByHostelIdAndCustomerIds(String hostelId, List<String> customerId);

    List<CustomersBedHistory> findByHostelIdAndCustomerId(String hostelId, String customerId);

    @Query(value = """
            SELECT * FROM customers_bed_history cbh
            WHERE cbh.customer_id IN (:customerId)
                AND DATE(cbh.start_date) <= DATE(:endDate)
                AND (cbh.end_date IS NULL OR DATE(cbh.end_date) >= DATE(:startDate))
            """, nativeQuery = true)
    List<CustomersBedHistory> findByListCustomerIdsAndStartAndEndDate(@Param("customerId") List<String> customerId,
                                                                      @Param("startDate") Date startDate,
                                                                      @Param("endDate") Date endDate);

    @Query(value = """
            SELECT * FROM customers_bed_history cbh
            WHERE cbh.customer_id = :customerId
                AND DATE(cbh.start_date) <= DATE(:endDate)
                AND (cbh.end_date IS NULL OR DATE(cbh.end_date) >= DATE(:startDate))
            """, nativeQuery = true)
    List<CustomersBedHistory> findByCustomerIdAndStartAndEndDate(@Param("customerId") String customerId,
                                                                 @Param("startDate") Date startDate,
                                                                 @Param("endDate") Date endDate);

    CustomersBedHistory findTopByCustomerIdOrderByCreatedAtDesc(String customerId);

    @Query("""
            SELECT cbh
            FROM CustomersBedHistory cbh
            WHERE cbh.id = (
                SELECT MAX(cbh2.id)
                FROM CustomersBedHistory cbh2
                WHERE cbh2.customerId = cbh.customerId
            )
            AND cbh.customerId IN :customerIds
            """)
    List<CustomersBedHistory> findLatestByCustomerIds(@Param("customerIds") Set<String> customerIds);

    @Query("""
            SELECT cbh FROM CustomersBedHistory cbh
            WHERE cbh.customerId = :customerId
                AND cbh.type IN ('CHECK_IN', 'REASSIGNED', 'RENT_REVISION')
                AND (cbh.endDate IS NULL OR DATE(cbh.endDate) >= DATE(:beforeDate))
            """)
    List<CustomersBedHistory> findAllByCustomerIdAndEndDateBefore(String customerId, Date beforeDate);

    List<CustomersBedHistory> findAllByCustomerIdAndTypeNot(String customerId, String type);
}
