package com.smartstay.console.Mapper.plans;

import com.smartstay.console.dao.Plans;
import com.smartstay.console.responses.plans.PlansDropdownRes;
import com.smartstay.console.utils.Utils;

import java.util.function.Function;

public class PlansDropdownResMapper implements Function<Plans, PlansDropdownRes> {

    @Override
    public PlansDropdownRes apply(Plans plans) {

        Double yearlyPrice = Utils.roundOfDoubleTo2Digits(plans.getFinalPrice() * 12);

        return new PlansDropdownRes(plans.getPlanId(), plans.getPlanName(), plans.getPlanCode(), plans.getPlanType(),
                plans.getDuration(), plans.getPrice(), plans.getDiscounts(), plans.getGst(), plans.getCgst(),
                plans.getSgst(), plans.getGstAmount(), plans.getCgstAmount(), plans.getSgstAmount(), plans.getFinalPrice(),
                yearlyPrice);
    }
}
