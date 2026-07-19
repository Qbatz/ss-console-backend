package com.smartstay.console.responses.customers;

import java.util.List;

public record CustomerWalletInfoRes(Double walletAmount,
                                    List<WalletHistoryRes> walletHistory) {
}
