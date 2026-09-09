package com.smartstay.console.responses.customers;

public record OtherInvoiceItemsRes(Long invoiceItemId,
                                   String invoiceItem,
                                   String otherItem,
                                   Double amount) {
}
