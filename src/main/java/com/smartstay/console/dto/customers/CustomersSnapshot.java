package com.smartstay.console.dto.customers;

import java.util.Date;

public record CustomersSnapshot(String customerId,
                                String xuid,
                                String firstName,
                                String lastName,
                                String mobile,
                                String emailId,
                                String houseNo,
                                String street,
                                String landmark,
                                int pincode,
                                String city,
                                String state,
                                Long country,
                                String profilePic,
                                String customerBedStatus,
                                Date joiningDate,
                                Date expJoiningDate,
                                Date dateOfBirth,
                                String currentStatus,
                                String gender,
                                String kycStatus,
                                String createdBy,
                                String hostelId,
                                Date createdAt,
                                Date lastUpdatedAt,
                                String updatedBy,
                                String mobSerialNo,

                                AdvanceSnapshot advance,
                                KycDetailsSnapshot kycDetails,
                                CustomerWalletSnapshot wallet,
                                ReasonsSnapshot reasons) {
}
