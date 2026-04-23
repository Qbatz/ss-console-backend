package com.smartstay.console.dto.customers;

import java.util.Date;

public record CustomerWalletSnapshot(Long walletId,
                                     Double amount,
                                     Date transactionDate,
                                     String customerId) {
}
