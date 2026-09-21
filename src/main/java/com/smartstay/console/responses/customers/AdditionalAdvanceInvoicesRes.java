package com.smartstay.console.responses.customers;

public record AdditionalAdvanceInvoicesRes(String invoiceId,
                                           String invoiceNumber,
                                           Double invoiceAmount,
                                           Double paidAmount,
                                           Double invoiceBalance) {
}
