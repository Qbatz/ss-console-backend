package com.smartstay.console.repositories;

import com.smartstay.console.dao.Plans;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlansRepository extends JpaRepository<Plans, Long> {
    Plans findPlanByPlanType(String planType);
    Plans findByPlanCode(String planCode);
}
