package com.smartstay.console.dto.invoice;

import java.util.Date;

public record InvoiceItemSnapshot(Long invoiceItemId,
                                  String invoiceItem,
                                  String otherItem,
                                  Double amount,
                                  Date fromDate,
                                  Date toDate,
                                  String invoiceId) {
}
