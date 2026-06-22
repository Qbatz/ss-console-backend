package com.smartstay.console.responses.customers;

import com.smartstay.console.responses.invoice.InvoiceResponse;
import com.smartstay.console.responses.invoiceRedemption.InvoiceRedemptionRes;
import com.smartstay.console.responses.transaction.TransactionResponse;

import java.util.List;

public record CustomerDetailsRes(String customerId,
                                 String firstName,
                                 String lastName,
                                 String fullName,
                                 String initials,
                                 String mobSerialNo,
                                 String mobile,
                                 String emailId,
                                 String houseNo,
                                 String street,
                                 String landMark,
                                 int pinCode,
                                 String city,
                                 String state,
                                 Long countryId,
                                 String fullAddress,
                                 String profilePic,
                                 String currentStatus,
                                 String customerBedStatus,
                                 String kycStatus,
                                 String joiningDate,
                                 String expJoiningDate,
                                 String dateOfBirth,
                                 String gender,
                                 String createdBy,
                                 String updatedBy,
                                 String createdAtDate,
                                 String createdAtTime,
                                 String updatedAtDate,
                                 String updatedAtTime,
                                 TenantHostelDetailsRes hostelDetails,
                                 List<InvoiceResponse> invoices,
                                 List<TransactionResponse> transactions,
                                 List<InvoiceRedemptionRes> invoiceRedemptions) {
}
