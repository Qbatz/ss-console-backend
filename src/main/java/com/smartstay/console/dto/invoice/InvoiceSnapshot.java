package com.smartstay.console.dto.invoice;

import com.smartstay.console.dto.customers.DeductionsSnapshot;

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
                              Double subTotal,
                              Double gst,
                              Double cgst,
                              Double sgst,
                              Double gstPercentile,
                              String paymentStatus,
                              Double deductionAmount,
                              String othersDescription,
                              String invoiceMode,
                              boolean isCancelled,
                              boolean isDiscounted,
                              List<String> cancelledInvoices,
                              List<DeductionsSnapshot> deductions,
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
