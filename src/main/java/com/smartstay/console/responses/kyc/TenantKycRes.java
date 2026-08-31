package com.smartstay.console.responses.kyc;

public record TenantKycRes(String customerId,
                           String firstName,
                           String lastName,
                           String fullName,
                           String initials,
                           String profilePic,
                           String mobile,
                           String emailId,
                           String joiningDate,
                           String billingCycleStart,
                           String billingCycleEnd,
                           String kycCompletedDate,
                           String kycCompletedTime,
                           String kycDetailsStatus,
                           boolean canSendReminder,
                           boolean canApproveKyc) {
}
