package com.smartstay.console.responses.customers;

public record CustomerSummaryResponse(String customerId,
                                      String firstName,
                                      String lastName,
                                      String fullName,
                                      String initials,
                                      String mobile,
                                      String emailId,
                                      String profilePic,
                                      String hostelId,
                                      String hostelName,
                                      String currentStatus,
                                      Double payableAmount,
                                      Double paidAmount,
                                      Double dueAmount) {
}
