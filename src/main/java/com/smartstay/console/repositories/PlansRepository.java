package com.smartstay.console.repositories;

import com.smartstay.console.dao.Plans;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlansRepository extends JpaRepository<Plans, Long> {

    Plans findPlanByPlanTypeAndIsActiveTrue(String planType);

    Plans findByPlanCodeAndIsActiveTrue(String planCode);

    @Query("""
                SELECT p FROM Plans p
                WHERE p.isActive = true
                  AND p.planId <> 1
                ORDER BY p.planId ASC
            """)
    List<Plans> findActivePlansExcludingTrial();

    Plans findByPlanIdAndIsActiveTrue(Long planId);
}
