package com.smartstay.console.repositories;

import com.smartstay.console.dao.Plans;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlansRepository extends JpaRepository<Plans, Long> {

    Plans findTopByPlanTypeAndIsActiveTrueOrderByPlanIdAsc(String planType);

    Plans findTopByPlanTypeAndIsActiveTrueOrderByPlanIdDesc(String planType);

    Plans findByPlanCodeAndIsActiveTrue(String planCode);

    @Query("""
                SELECT p FROM Plans p
                WHERE p.isActive = true
                  AND p.planType <> 'TRIAL'
                ORDER BY p.planId ASC
            """)
    List<Plans> findActivePlansExcludingTrial();

    Plans findByPlanIdAndIsActiveTrue(Long planId);

    boolean existsByPlanCodeIgnoreCase(String planCode);

    boolean existsByPlanNameIgnoreCase(String planName);

    boolean existsByPlanTypeIgnoreCase(String planType);

    boolean existsByPlanCodeIgnoreCaseAndPlanIdNot(String planCode, Long planId);

    boolean existsByPlanNameIgnoreCaseAndPlanIdNot(String planName, Long planId);

    boolean existsByPlanTypeIgnoreCaseAndPlanIdNot(String planType, Long planId);
}
