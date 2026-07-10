package com.smartstay.console.responses.customers;

public record WalletHistoryRes(Long walletHistoryId,
                               double amount,
                               String sourceId,
                               String source,
                               String defaultSourceType,
                               String billStartDate,
                               String billEndDate) {
}
