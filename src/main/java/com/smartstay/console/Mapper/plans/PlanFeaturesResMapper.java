package com.smartstay.console.Mapper.plans;

import com.smartstay.console.dao.PlanFeatures;
import com.smartstay.console.responses.plans.PlanFeaturesResponse;

import java.util.function.Function;

public class PlanFeaturesResMapper implements Function<PlanFeatures, PlanFeaturesResponse> {

    @Override
    public PlanFeaturesResponse apply(PlanFeatures planFeatures) {
        return new PlanFeaturesResponse(planFeatures.getId(), planFeatures.getFeatureName(),
                planFeatures.getPrice());
    }
}
