package com.smartstay.console.responses.kyc;

import java.util.List;

public record KycTenantRes(String hostelId,
                           String hostelName,
                           String initials,
                           String mainImage,
                           String mobile,
                           String emailId,
                           String fullAddress,
                           long totalTenants,
                           long totalKycRequestedTenants,
                           long totalRequested,
                           long totalVerified,
                           long totalWaitingForApproval,
                           boolean kycEnableStatus,
                           int kycLimitPerMonth,
                           List<TenantKycRes> tenants) {
}
