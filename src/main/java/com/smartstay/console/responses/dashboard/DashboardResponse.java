package com.smartstay.console.responses.dashboard;

public record DashboardResponse(long hostelCount,
                                long ownersCount,
                                long agentCount,
                                long demoRequestCount,
                                long expiredSubscriptionsCount) {
}
