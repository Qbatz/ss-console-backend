package com.smartstay.console.dto.transaction;

import java.util.Date;

public record TransactionSnapshot(String transactionId,
                                  String type,
                                  Double paidAmount,
                                  String createdBy,
                                  Date createdAt,
                                  String status,
                                  String invoiceId,
                                  String hostelId,
                                  String isInvoice,
                                  String customerId,
                                  Date paymentDate,
                                  String transactionMode,
                                  String transactionReferenceId,
                                  String receiptUrl,
                                  String bankId,
                                  String referenceNumber,
                                  Date paidAt,
                                  String updatedBy) {
}
