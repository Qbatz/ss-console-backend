package com.smartstay.console.responses.plans;

import java.util.List;

public record PlansDropdownWrapper(List<PlansDropdownRes> trialPlans,
                                   List<PlansDropdownRes> expandableTrialPlans,
                                   List<PlansDropdownRes> otherPlans) {
}
