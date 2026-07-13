package com.smartstay.console.Mapper.plans;

import com.smartstay.console.dao.PlanFeatures;
import com.smartstay.console.dao.Plans;
import com.smartstay.console.dao.SmartstayFeatures;
import com.smartstay.console.dto.plans.PlanFeatureDto;
import com.smartstay.console.responses.plans.PlanFeaturesResponse;
import com.smartstay.console.responses.plans.PlansResponse;
import com.smartstay.console.services.PlansService;
import com.smartstay.console.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class PlanResMapper implements Function<Plans, PlansResponse> {

    List<SmartstayFeatures> allSmartstayFeatures;
    List<SmartstayFeatures> commonFeatures;
    List<PlanFeatures> planFeatures;

    public PlanResMapper(List<SmartstayFeatures> allSmartstayFeatures,
                         List<SmartstayFeatures> commonFeatures,
                         List<PlanFeatures> planFeatures) {
        this.allSmartstayFeatures = allSmartstayFeatures;
        this.commonFeatures = commonFeatures;
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

        List<PlanFeaturesResponse> planFeaturesRes = new ArrayList<>();
        if (commonFeatures != null && planFeatures != null && allSmartstayFeatures != null) {

            List<PlanFeatureDto> mergedFeatures = PlansService
                    .mergeFeatures(allSmartstayFeatures, commonFeatures, planFeatures);

            planFeaturesRes = mergedFeatures.stream()
                    .map(planFeature -> new PlanFeaturesResponse(planFeature.planFeatureId(),
                            planFeature.smartStayFeatureId(), planFeature.featureName(), planFeature.isCommon(),
                            planFeature.price(), planFeature.isFeatureActive(), planFeature.labelText(),
                            planFeature.labelDescription(), planFeature.startsFrom(), planFeature.endsAt()))
                    .toList();
        }

        double yearlyPrice = plans.getFinalPrice() * 12;
        yearlyPrice = yearlyPrice - (yearlyPrice * 20 / 100);
        yearlyPrice = Utils.roundOfDoubleTo2Digits(yearlyPrice);

        return new PlansResponse(plans.getPlanId(), plans.getPlanName(), plans.getPlanCode(),
                plans.getPlanType(), plans.getDuration(), plans.getPrice(), plans.getDiscounts(),
                plans.getGst(), plans.getCgst(), plans.getSgst(), plans.getGstAmount(),
                plans.getCgstAmount(), plans.getSgstAmount(), plans.getFinalPrice(), yearlyPrice,
                plans.isShouldShow(), plans.isCanCustomize(), createdAtDate, createdAtTime,
                updatedAtDate, updatedAtTime, planFeaturesRes);
    }
}
