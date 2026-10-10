package com.smartstay.console.responses.hostels;

public record HostelGraphRegionDataRes(String region,
                                       long regionHostelCount,
                                       int regionPercentage) {
}
