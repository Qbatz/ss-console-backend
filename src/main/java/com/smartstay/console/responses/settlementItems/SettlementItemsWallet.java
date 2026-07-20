package com.smartstay.console.responses.settlementItems;

public record SettlementItemsWallet(Long walletId,
                                    String type,
                                    Double amount) {
}
