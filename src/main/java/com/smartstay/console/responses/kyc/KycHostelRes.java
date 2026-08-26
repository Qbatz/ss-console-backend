package com.smartstay.console.responses.kyc;

public record KycHostelRes(String hostelId,
                           String hostelName,
                           String initials,
                           String mainImage,
                           String mobile,
                           String emailId,
                           String fullAddress,
                           long totalTenants,
                           long totalVerifiedTenants,
                           String latestRequestTo,
                           String latestCompletionBy,
                           long totalRequests,
                           long totalCompleted,
                           boolean kycEnableStatus,
                           String latestRequestDate,
                           String latestRequestTime,
                           String latestCompletionDate,
                           String latestCompletionTime,
                           String lastUpdatedDate,
                           String lastUpdatedTime) {
}
