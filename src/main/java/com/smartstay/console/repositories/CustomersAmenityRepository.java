package com.smartstay.console.repositories;

import com.smartstay.console.dao.CustomersAmenity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomersAmenityRepository extends JpaRepository<CustomersAmenity, String> {
    List<CustomersAmenity> findByCustomerIdIn( List<String> customerIds);
}
