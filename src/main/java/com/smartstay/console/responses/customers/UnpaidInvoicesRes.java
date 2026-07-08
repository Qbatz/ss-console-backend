package com.smartstay.console.responses.customers;

public record UnpaidInvoicesRes(String invoiceNumber,
                                String invoiceId,
                                String type,
                                Double invoiceTotalAmount,
                                Double payableAmount,
                                Double ebAmount,
                                Double amenityAmount) {
}
