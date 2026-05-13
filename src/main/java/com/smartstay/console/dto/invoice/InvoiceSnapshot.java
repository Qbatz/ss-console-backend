package com.smartstay.console.dto.invoice;

import java.util.Date;
import java.util.List;

public record InvoiceSnapshot(String invoiceId,
                              String customerId,
                              String hostelId,
                              String invoiceNumber,
                              String customerMobile,
                              String customerMailId,
                              String invoiceType,
                              Double basePrice,
                              Double totalAmount,
                              Double paidAmount,
                              Double balanceAmount,
                              Double gst,
                              Double cgst,
                              Double sgst,
                              Double gstPercentile,
                              String paymentStatus,
                              String othersDescription,
                              String invoiceMode,
                              boolean isCancelled,
                              boolean isDiscounted,
                              List<String> cancelledInvoices,
                              String invoiceUrl,
                              String createdBy,
                              String updatedBy,
                              Date invoiceGeneratedDate,
                              Date invoiceDueDate,
                              Date invoiceStartDate,
                              Date invoiceEndDate,
                              Date createdAt,
                              Date updatedAt,
                              List<InvoiceItemSnapshot> invoiceItems) {
}
