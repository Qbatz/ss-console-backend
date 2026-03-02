package com.smartstay.console.repositories;

import com.smartstay.console.dao.AmenityRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AmenityRequestRepository extends JpaRepository<AmenityRequest, Long> {
    @Query("""
            SELECT ar FROM AmenityRequest ar WHERE ar.hostelId=:hostelId AND ar.customerId IN (:customerId)
            """)
    public List<AmenityRequest> findByHostelIdAndCustomerIdIn(String hostelId, List<String> customerId);
}
