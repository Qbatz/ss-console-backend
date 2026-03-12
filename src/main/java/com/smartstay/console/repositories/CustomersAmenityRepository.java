package com.smartstay.console.repositories;

import com.smartstay.console.dao.CustomersAmenity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface CustomersAmenityRepository extends JpaRepository<CustomersAmenity, String> {
    List<CustomersAmenity> findByCustomerIdIn( List<String> customerIds);

    List<CustomersAmenity> findByCustomerId(String customerId);

    @Query(value = """
        SELECT * FROM customers_amenity WHERE customer_id=:customerId AND (end_date IS NULL OR DATE(end_date) >= DATE(:date))
        """, nativeQuery = true)
    List<CustomersAmenity> getAllCustomersAmenityByCustomerIdAndEndDate(@Param("customerId") String customerId,
                                                                        @Param("date") Date date);
}
