package com.smartstay.console.Mapper.plans;

import com.smartstay.console.dao.Plans;
import com.smartstay.console.responses.plans.PlanFeaturesResponse;
import com.smartstay.console.responses.plans.PlansResponse;
import com.smartstay.console.utils.Utils;

import java.util.List;
import java.util.function.Function;

public class PlanResMapper implements Function<Plans, PlansResponse> {

    @Override
    public PlansResponse apply(Plans plans) {

        String createdAtDate = null;
        String createdAtTime = null;
        if (plans.getCreatedAt() != null) {
            createdAtDate = Utils.dateToString(plans.getCreatedAt());
            createdAtTime = Utils.dateToTime(plans.getCreatedAt());
        }

        String updatedAtDate = null;
        String updatedAtTime = null;
        if (plans.getUpdatedAt() != null) {
            updatedAtDate = Utils.dateToString(plans.getUpdatedAt());
            updatedAtTime = Utils.dateToTime(plans.getUpdatedAt());
        }

        List<PlanFeaturesResponse> planFeatures = plans.getFeaturesList().stream()
                .map(planFeature -> new PlanFeaturesResMapper().apply(planFeature))
                .toList();

        return new PlansResponse(plans.getPlanId(), plans.getPlanName(), plans.getPlanCode(),
                plans.getPlanType(), plans.getDuration(), plans.getPrice(), plans.getDiscounts(),
                plans.isShouldShow(), plans.isCanCustomize(), createdAtDate, createdAtTime,
                updatedAtDate, updatedAtTime, planFeatures);
    }
}
