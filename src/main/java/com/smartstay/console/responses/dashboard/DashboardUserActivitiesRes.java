package com.smartstay.console.responses.dashboard;

public record DashboardUserActivitiesRes(String description,
                                         String activityType,
                                         String source,
                                         String hostelId,
                                         String hostelName,
                                         String hostelInitials,
                                         String hostelMainImage,
                                         String hostelAddress,
                                         String hostelCity,
                                         String dateDisplay) {
}
