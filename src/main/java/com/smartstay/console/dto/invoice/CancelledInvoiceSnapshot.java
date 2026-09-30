package com.smartstay.console.dto.invoice;

public record CancelledInvoiceSnapshot(String invoiceId,
                                       String paymentStatus) {
}
