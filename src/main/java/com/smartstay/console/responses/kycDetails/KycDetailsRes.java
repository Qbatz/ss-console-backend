package com.smartstay.console.responses.kycDetails;

public record KycDetailsRes(Long kycDetailsId,
                            String customerId,
                            String firstName,
                            String lastName,
                            String fullName,
                            String mobile,
                            String kycStatus,
                            String hostelId,
                            String hostelName,
                            String hostelMobile,
                            String hostelInitials,
                            String hostelMainImage,
                            String currentStatus,
                            String expiringAtDate,
                            String expiringAtTime,
                            String createdAtDate,
                            String createdAtTime,
                            String updatedAtDate,
                            String updatedAtTime) {
}
