package com.smartstay.console.responses.settlementItems;

public record SettlementItemsUnpaidInvoices(String invoiceNo,
                                            Double invoiceAmount,
                                            String invoiceType,
                                            String invoiceId,
                                            Double pendingAmount) {
}
