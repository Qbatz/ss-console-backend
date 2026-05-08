package com.smartstay.console.responses.invoice;

public record InvoiceItemsResponse(Long invoiceItemId,
                                   String invoiceItem,
                                   String otherItem) {
}
