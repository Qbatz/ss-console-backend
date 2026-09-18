package com.smartstay.console.responses.customers;

import java.util.List;

public record OtherInvoicesRes(String invoiceId,
                               String invoiceNumber,
                               String invoiceDate,
                               Double invoiceAmount,
                               Double paidAmount,
                               Double pendingAmount,
                               List<OtherInvoiceItemsRes> otherInvoiceItems) {
}
