package com.smartstay.console.repositories;

import com.smartstay.console.dao.CustomersConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomersConfigRepository extends JpaRepository<CustomersConfig, Long> {
    @Query("""
            SELECT cc FROM CustomersConfig cc WHERE cc.hostelId=:hostelId AND cc.customerId IN (:customerIds)
            """)
    List<CustomersConfig> findByHostelIdAndCustomerIds(String hostelId, List<String> customerIds);

    List<CustomersConfig> findByHostelIdAndCustomerId(String hostelId, String customerId);

    @Query(value = """
        SELECT * FROM customers_config WHERE hostel_id=:hostelId AND is_active=true AND enabled=true
        """, nativeQuery = true)
    List<CustomersConfig> findActiveAndRecurringEnabledCustomersByHostelId(@Param("hostelId") String hostelId);
}
