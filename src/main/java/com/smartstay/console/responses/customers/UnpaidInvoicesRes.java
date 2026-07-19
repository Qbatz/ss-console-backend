package com.smartstay.console.responses.customers;

public record UnpaidInvoicesRes(String invoiceId,
                                String invoiceNumber,
                                String type,
                                String dbInvoiceType,
                                Double invoiceTotalAmount,
                                Double invoicePaidAmount,
                                Double pendingAmount,
                                Double ebAmount,
                                Double amenityAmount) {
}
