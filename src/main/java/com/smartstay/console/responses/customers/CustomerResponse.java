package com.smartstay.console.responses.customers;

public record CustomerResponse(String customerId,
                               String firstName,
                               String lastName,
                               String fullName,
                               String initials,
                               String mobile,
                               String emailId,
                               String currentStatus,
                               String joiningDate,
                               String kycStatus,
                               String kycDetailsStatus,
                               boolean canApproveKyc,
                               boolean canGenerateSettlement) {
}
