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
            SELECT * FROM customers_amenity
            WHERE customer_id = :customerId
                AND (end_date IS NULL OR DATE(end_date) >= DATE(:date))
            """, nativeQuery = true)
    List<CustomersAmenity> getAllCustomersAmenityByCustomerIdAndEndDate(@Param("customerId") String customerId,
                                                                        @Param("date") Date date);

    @Query("""
            SELECT ca FROM CustomersAmenity ca
            WHERE ca.customerId = :customerId
                AND (DATE(ca.startDate) <=  DATE(:date))
                AND (ca.endDate IS NULL OR DATE(ca.endDate) >= DATE(:date))
            """)
    List<CustomersAmenity> findAllByCustomerIdAndDateBetween(String customerId, Date date);
}
