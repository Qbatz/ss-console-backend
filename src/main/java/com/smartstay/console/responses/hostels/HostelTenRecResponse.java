package com.smartstay.console.responses.hostels;

import com.smartstay.console.responses.customers.CustomerRecurringResponse;

import java.util.List;

public record HostelTenRecResponse(String hostelId,
                                   String hostelType,
                                   String hostelName,
                                   String initials,
                                   String mobile,
                                   String emailId,
                                   String houseNo,
                                   String street,
                                   String landmark,
                                   String city,
                                   String state,
                                   int country,
                                   int pincode,
                                   String fullAddress,
                                   String mainImage,
                                   OwnerInfo ownerInfo,
                                   int activeTenantCount,
                                   int invoiceAboutToBeGeneratedCount,
                                   String billingType,
                                   String billingModel,
                                   boolean isSubscriptionActive,
                                   List<CustomerRecurringResponse> customerList) {
}
