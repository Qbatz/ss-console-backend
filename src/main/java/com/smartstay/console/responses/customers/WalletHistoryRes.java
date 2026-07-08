package com.smartstay.console.responses.customers;

public record WalletHistoryRes(Long walletId,
                               double amount,
                               String source,
                               String sourceId,
                               String billStartDate,
                               String billEndDate) {
}
