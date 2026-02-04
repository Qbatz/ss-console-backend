package com.smartstay.console.responses.hostels;

public record HostelList(String hostelName,
                         String hostelId,
                         String hostelImage,
                         String initials,
                         boolean subscriptionIsActive,
                         long noOfdaysSubscriptionActive,
                         String lastUpdateDate,
                         String lastUpdateTime,
                         OwnerInfo ownerInfo,
                         HostelPlan hostelPlan) {
}
