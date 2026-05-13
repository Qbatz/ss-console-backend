package com.smartstay.console.responses.plans;

import java.util.List;

public record PlansResponseWrapper(List<PlansResponse> activePlans,
                                   List<PlansResponse> inActivePlans) {
}
