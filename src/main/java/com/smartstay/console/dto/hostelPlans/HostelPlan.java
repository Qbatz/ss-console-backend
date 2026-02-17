package com.smartstay.console.dto.hostelPlans;

import java.util.Date;

public record HostelPlan(String hostelId, Date startDate, Date endDate, String planCode, String planName) {
}
