package com.smartstay.console.Mapper.plans;

import com.smartstay.console.dao.Plans;
import com.smartstay.console.dto.plans.PlanFeatureDto;
import com.smartstay.console.responses.plans.PlanFeaturesResponse;
import com.smartstay.console.responses.plans.PlansResponse;
import com.smartstay.console.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class InActivePlanResMapper implements Function<Plans, PlansResponse> {

    List<PlanFeatureDto> planFeatures;

    public InActivePlanResMapper(List<PlanFeatureDto> planFeatures) {
        this.planFeatures = planFeatures;
    }

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

        List<PlanFeaturesResponse> planFeaturesResponses = new ArrayList<>();
        if (planFeatures != null) {
            planFeaturesResponses = planFeatures.stream()
                    .map(planFeature -> new PlanFeaturesResponse(planFeature.smartStayFeatureId(),
                            planFeature.featureName(), planFeature.price(), planFeature.isFeatureActive()))
                    .toList();
        }

        Double yearlyPrice = Utils.roundOfDoubleTo2Digits(plans.getFinalPrice() * 12);

        return new PlansResponse(plans.getPlanId(), plans.getPlanName(), plans.getPlanCode(),
                plans.getPlanType(), plans.getDuration(), plans.getPrice(), plans.getDiscounts(),
                plans.getGst(), plans.getCgst(), plans.getSgst(), plans.getGstAmount(),
                plans.getCgstAmount(), plans.getSgstAmount(), plans.getFinalPrice(), yearlyPrice,
                plans.isShouldShow(), plans.isCanCustomize(), createdAtDate, createdAtTime,
                updatedAtDate, updatedAtTime, planFeaturesResponses);
    }
}
