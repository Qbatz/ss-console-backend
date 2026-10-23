package com.smartstay.console.responses.dashboard;

public record DashboardOwnerDataRes(String ownerId,
                                    String ownerName,
                                    long hostelCount,
                                    long cityCount) {
}
