package com.smartstay.console.dto.customers;

public record KycDetailsSnapshot(int id,
                                 String currentStatus,
                                 String transactionId,
                                 String referenceId,
                                 String customerId) {
}
