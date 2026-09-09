package com.smartstay.console.responses.customers;

import java.util.List;

public record OtherInvoicesInfoRes(Double totalInvoiceAmount,
                                   Double totalPaidAmount,
                                   Double totalPendingAmount,
                                   int totalInvoice,
                                   List<OtherInvoicesRes> otherInvoices) {
}
