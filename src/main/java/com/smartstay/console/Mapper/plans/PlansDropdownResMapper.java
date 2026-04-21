package com.smartstay.console.Mapper.plans;

import com.smartstay.console.dao.Plans;
import com.smartstay.console.responses.plans.PlansDropdownRes;

import java.util.function.Function;

public class PlansDropdownResMapper implements Function<Plans, PlansDropdownRes> {

    @Override
    public PlansDropdownRes apply(Plans plans) {
        return new PlansDropdownRes(plans.getPlanId(), plans.getPlanName(), plans.getPlanCode(), plans.getPlanType(),
                plans.getDuration(), plans.getPrice(), plans.getDiscounts());
    }
}
