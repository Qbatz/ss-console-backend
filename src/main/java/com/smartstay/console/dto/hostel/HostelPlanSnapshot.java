package com.smartstay.console.dto.hostel;

import java.util.Date;

public record HostelPlanSnapshot(Long hostelPlanId,
                                 String currentPlanCode,
                                 String currentPlanName,
                                 Date currentPlanStartsAt,
                                 Date currentPlanEndsAt,
                                 Double currentPlanPrice,
                                 Double paidAmount,
                                 boolean isTrial,
                                 Date trialEndingAt,
                                 String hostelId) {
}
