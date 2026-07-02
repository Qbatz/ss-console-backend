package com.smartstay.console.repositories;

import com.smartstay.console.dao.KycDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface KycDetailsRepository extends JpaRepository<KycDetails, Long> {

    @Query("""
            select kd
            from KycDetails kd
            where (:customerIds is null or kd.customers.customerId in :customerIds)
                and kd.currentStatus = :status
            """)
    Page<KycDetails> findPaginatedKycDetailsAndKycStatusIn(Set<String> customerIds,
                                                           String status,
                                                           Pageable pageable);
}
