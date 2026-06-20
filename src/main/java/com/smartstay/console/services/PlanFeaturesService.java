package com.smartstay.console.services;

import com.smartstay.console.dao.PlanFeatures;
import com.smartstay.console.repositories.PlanFeaturesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class PlanFeaturesService {

    @Autowired
    private PlanFeaturesRepository planFeaturesRepository;

    public List<PlanFeatures> saveAll(List<PlanFeatures> updatedPlanFeatures) {
        return planFeaturesRepository.saveAll(updatedPlanFeatures);
    }

    public PlanFeatures save(PlanFeatures planFeatures) {
        return planFeaturesRepository.save(planFeatures);
    }

    public List<PlanFeatures> findAllByPlanIds(Set<Long> planIds){
        return planFeaturesRepository.findAllByPlan_PlanIdInAndIsActiveTrue(planIds);
    }

    public List<PlanFeatures> getBySmartstayFeatureIdsAndPlanId(Set<Long> smartstayFeatureIds, Long planId) {
        return planFeaturesRepository.findAllBySmartstayFeatureIdInAndPlan_PlanId(smartstayFeatureIds, planId);
    }

    public List<PlanFeatures> getBySmartstayFeatureId(Long smartstayFeatureId) {
        return planFeaturesRepository.findAllBySmartstayFeatureIdAndIsActiveTrue(smartstayFeatureId);
    }

    public void updatePlanFeatureNameBySmartstayFeatureId(Long smartstayFeatureId,
                                                          String featureName) {

        List<PlanFeatures> planFeatures = planFeaturesRepository
                .findAllBySmartstayFeatureId(smartstayFeatureId);

        if (planFeatures.isEmpty()) {
            return;
        }

        planFeatures.forEach(pf -> pf.setFeatureName(featureName));

        planFeaturesRepository.saveAll(planFeatures);
    }
}
