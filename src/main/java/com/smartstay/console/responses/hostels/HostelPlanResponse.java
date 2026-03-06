package com.smartstay.console.responses.hostels;

public record HostelPlanResponse(String planCode,
                                 String planName,
                                 String planStartsAt,
                                 String planEndsAt,
                                 Double planAmount,
                                 boolean isPlanActive) {
}
