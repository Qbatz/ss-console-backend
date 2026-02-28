package com.smartstay.console.repositories;

import com.smartstay.console.dao.ComplaintsV1;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintsRepository extends JpaRepository<ComplaintsV1, Integer> {

    @Query("""
            SELECT c FROM ComplaintsV1 c WHERE c.hostelId=:hostelId AND c.customerId IN (:customerIds)
            """)
    List<ComplaintsV1> findByHostelIdAndCustomerIdIn(String hostelId, List<String> customerIds);
}
