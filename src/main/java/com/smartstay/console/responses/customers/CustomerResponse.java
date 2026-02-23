package com.smartstay.console.responses.customers;

public record CustomerResponse(String customerId,
                               String firstName,
                               String lastName,
                               String fullName,
                               String initials,
                               String mobile,
                               String emailId,
                               String currentStatus) {
}
