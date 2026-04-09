package com.smartstay.console.repositories;

import com.smartstay.console.dao.CustomersEbHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerEbHistoryRepository extends JpaRepository<CustomersEbHistory, Long> {

    List<CustomersEbHistory> findByCustomerIdIn(List<String> customerIds);

    List<CustomersEbHistory> findByCustomerId(String customerId);

    @Query("""
        SELECT CER FROM CustomersEbHistory CER WHERE CER.customerId=:customerId AND CER.readingId IN :readings
        """)
    List<CustomersEbHistory> findByCustomerIdAndReadingsId(@Param("customerId") String customerId,
                                                           @Param("readings") List<Integer> readings);
}
