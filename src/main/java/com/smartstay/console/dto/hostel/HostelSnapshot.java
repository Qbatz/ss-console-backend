package com.smartstay.console.dto.hostel;

import java.util.Date;
import java.util.List;

public record HostelSnapshot(String hostelId,
                             int hostelType,
                             String hostelName,
                             String mobile,
                             String emailId,
                             String mainImage,
                             String houseNo,
                             String street,
                             String landmark,
                             int pincode,
                             String city,
                             String state,
                             int country,
                             String parentId,

                             String createdBy,
                             Date createdAt,
                             Date updatedAt,

                             boolean isActive,
                             boolean isDeleted,

                             HostelPlanSnapshot hostelPlan,
                             ElectricityConfigSnapshot electricityConfig,
                             List<HostelImageSnapshot> additionalImages,
                             List<BillingRuleSnapshot> billingRulesList) {
}
