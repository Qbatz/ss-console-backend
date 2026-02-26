package com.smartstay.console.responses.hostels;

public record HostelList(String hostelName,
                         String hostelId,
                         String hostelImage,
                         String initials,
                         String countryCode,
                         String mobile,
                         String fullAddress,
                         String city,
                         String state,
                         String joinedOn,
                         String expiredOn,
                         String expiringAt,
                         boolean isTrial,
                         boolean subscriptionIsActive,
                         long noOfdaysSubscriptionActive,
                         String lastUpdateDate,
                         String lastUpdateTime,
                         OwnerInfo ownerInfo,
                         HostelPlan hostelPlan) {
}
