package com.smartstay.console.repositories;

import com.smartstay.console.dao.PlanFeatures;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface PlanFeaturesRepository extends JpaRepository<PlanFeatures, Long> {

    List<PlanFeatures> findAllByPlan_PlanIdInAndIsActiveTrue(Set<Long> planIds);

    List<PlanFeatures> findAllByPlan_PlanIdAndIsActiveTrue(Long planId);

    List<PlanFeatures> findAllBySmartstayFeatureIdInAndPlan_PlanId(Set<Long> smartstayFeatureIds, Long planId);

    List<PlanFeatures> findAllBySmartstayFeatureIdAndIsActiveTrue(Long smartstayFeatureId);

    List<PlanFeatures> findAllBySmartstayFeatureId(Long smartstayFeatureId);
}
