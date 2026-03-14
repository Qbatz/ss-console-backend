package com.smartstay.console.dto.hostelPlans;

import java.util.Date;

public record HostelPlanProjection(String parentId,
                                   Date currentPlanEndsAt) {
}
