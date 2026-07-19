package com.smartstay.console.responses.transaction;

import java.util.List;

public record TransactionResWrapper(boolean canUpdateInvoiceBalance,
                                    List<TransactionResponse> transactions) {
}
