package com.smartstay.console.utils;

import com.smartstay.console.dao.PlanFeatures;
import com.smartstay.console.dao.Plans;

import java.util.Date;

public class CloneUtility {

    public static Plans clonePlans(Plans oldPlans) {
        Plans copy = new Plans();

        copy.setPlanId(oldPlans.getPlanId());
        copy.setPlanName(oldPlans.getPlanName());
        copy.setPrice(oldPlans.getPrice());
        copy.setDuration(oldPlans.getDuration());
        copy.setDiscounts(oldPlans.getDiscounts());
        copy.setPlanType(oldPlans.getPlanType());
        copy.setPlanCode(oldPlans.getPlanCode());
        copy.setShouldShow(oldPlans.isShouldShow());
        copy.setCanCustomize(oldPlans.isCanCustomize());
        copy.setActive(oldPlans.isActive());

        copy.setCreatedAt(
                oldPlans.getCreatedAt() != null ? new Date(oldPlans.getCreatedAt().getTime()) : null
        );

        copy.setUpdatedAt(
                oldPlans.getUpdatedAt() != null ? new Date(oldPlans.getUpdatedAt().getTime()) : null
        );

        copy.setFeaturesList(
                oldPlans.getFeaturesList() == null ? null :
                        oldPlans.getFeaturesList().stream().map(feature -> {
                            PlanFeatures f = new PlanFeatures();
                            f.setId(feature.getId());
                            f.setFeatureName(feature.getFeatureName());
                            f.setPrice(feature.getPrice());
                            f.setActive(feature.isActive());
                            return f;
                        }).toList()
        );

        return copy;
    }

    public static PlanFeatures clonePlanFeatures(PlanFeatures oldPlanFeatures) {
        PlanFeatures copy = new PlanFeatures();

        copy.setId(oldPlanFeatures.getId());
        copy.setFeatureName(oldPlanFeatures.getFeatureName());
        copy.setPrice(oldPlanFeatures.getPrice());
        copy.setActive(oldPlanFeatures.isActive());

        copy.setPlan(oldPlanFeatures.getPlan());

        return copy;
    }
}
