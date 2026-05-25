package com.smartstay.console.repositories;

import com.smartstay.console.dao.Plans;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface PlansRepository extends JpaRepository<Plans, Long> {

    Plans findByPlanCodeAndIsActiveTrue(String planCode);

    List<Plans> findByPlanCodeInAndIsActiveTrue(Set<String> planCodes);

    @Query("""
                SELECT p FROM Plans p
                WHERE p.planType <> 'TRIAL'
                  AND p.planType <> 'EXPANDABLE_TRIAL'
                ORDER BY p.planId ASC
            """)
    List<Plans> findPlansExcludingTrial();

    Plans findByPlanIdAndIsActiveTrue(Long planId);

    Plans findByPlanIdAndIsActiveFalse(Long planId);

    List<Plans> findByPlanTypeInAndIsActiveTrue(List<String> planTypes);

    boolean existsByPlanCodeIgnoreCase(String planCode);

    boolean existsByPlanNameIgnoreCase(String planName);

    boolean existsByPlanTypeIgnoreCase(String planType);

    boolean existsByPlanCodeIgnoreCaseAndPlanIdNot(String planCode, Long planId);

    boolean existsByPlanNameIgnoreCaseAndPlanIdNot(String planName, Long planId);

    boolean existsByPlanTypeIgnoreCaseAndPlanIdNot(String planType, Long planId);

    List<Plans> findAllByIsActiveTrue();

    List<Plans> findAllByPlanType(String planType);

    List<Plans> findAllByPlanTypeAndIsActiveTrue(String planType);

    Plans findTopByPlanTypeAndIsActiveTrueOrderByPlanIdAsc(String planType);

    List<Plans> findAllByPlanTypeNotIn(Set<String> planTypes);
}
