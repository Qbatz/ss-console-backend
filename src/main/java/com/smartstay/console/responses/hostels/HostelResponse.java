package com.smartstay.console.responses.hostels;

import com.smartstay.console.responses.bills.BillingRulesResponse;
import com.smartstay.console.responses.users.UsersResponse;

import java.util.List;

public record HostelResponse(String hostelId,
                             String hostelName,
                             String initials,
                             String mobile,
                             String houseNo,
                             String street,
                             String landmark,
                             String city,
                             String state,
                             int country,
                             int pincode,
                             String fullAddress,
                             String mainImage,
                             List<HostelImagesResponse> additionalImages,
                             int noOfFloors,
                             int noOfRooms,
                             int noOfBeds,
                             int noOfTenants,
                             String createdAtDate,
                             String createdAtTime,
                             OwnerInfo ownerInfo,
                             List<UsersResponse> masters,
                             List<UsersResponse> staffs,
                             HostelPlan hostelPlan,
                             List<BillingRulesResponse> billingRules,
                             EbConfig ebConfig,
                             List<SubscriptionResponse> subscriptions
) {
}
