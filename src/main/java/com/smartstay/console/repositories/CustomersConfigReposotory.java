package com.smartstay.console.repositories;

import com.smartstay.console.dao.CustomersConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomersConfigReposotory extends JpaRepository<CustomersConfig, Long> {
    @Query("""
            SELECT cc FROM CustomersConfig cc WHERE cc.hostelId=:hostelId AND cc.customerId IN (:customerIds) 
            """)
    List<CustomersConfig> findByHostelIdAndCustomerIds(String hostelId, List<String> customerIds);
}
