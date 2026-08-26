package com.smartstay.console.responses.kyc;

public record TenantKycRes(String customerId,
                           String firstName,
                           String lastName,
                           String fullName,
                           String joiningDate,
                           String billingCycleStart,
                           String billingCycleEnd,
                           String latestRequestDate,
                           String latestRequestTime,
                           String latestCompletionDate,
                           String latestCompletionTime,
                           String kycDetailsStatus,
                           boolean canSendReminder,
                           boolean canApproveKyc) {
}
