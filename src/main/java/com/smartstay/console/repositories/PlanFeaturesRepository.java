package com.smartstay.console.repositories;

import com.smartstay.console.dao.PlanFeatures;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface PlanFeaturesRepository extends JpaRepository<PlanFeatures, Long> {

    List<PlanFeatures> findAllByIdInAndIsActiveTrue(Set<Long> ids);

    PlanFeatures findByIdAndIsActiveTrue(Long id);
}
