package com.smartstay.console.responses.dashboard;

import java.util.List;

public record DashboardResponse(long hostelCount,
                                long activeHostelCount,
                                long ownersCount,
                                long agentCount,
                                long demoRequestCount,
                                long expiredSubscriptionsCount,
                                long bedCount,
                                long paidHostelCount,
                                long activePaidHostelCount,
                                long multiBranchOwnerCount,
                                long usedLast45DaysCount,
                                List<DashboardRegionGraphDateFilterRes> regionGraphDateFilters,
                                List<DashboardRegionGraphRes> regionGraphData,
                                List<DashboardRegionDataRes> regionData) {
}
