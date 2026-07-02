package com.smartstay.console.dto.customers;

public record KycDetailsSnapshot(Long id,
                                 String currentStatus,
                                 String transactionId,
                                 String referenceId,
                                 String customerId) {
}
