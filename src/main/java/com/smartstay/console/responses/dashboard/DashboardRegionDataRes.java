package com.smartstay.console.responses.dashboard;

public record DashboardRegionDataRes(String city,
                                     String state,
                                     long ownerCount,
                                     long hostelCount,
                                     String latestDateDisplay) {
}
