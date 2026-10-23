package com.smartstay.console.responses.hostels;

public record HostelGraphCardDataRes(long hostelCount,
                                     long onboardedThisMonth,
                                     double onboardedPercentageDifference,
                                     String topRegion,
                                     long topRegionHostelCount,
                                     int topRegionPercentage) {
}
