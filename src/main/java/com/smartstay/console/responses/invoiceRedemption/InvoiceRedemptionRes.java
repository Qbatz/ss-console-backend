package com.smartstay.console.responses.invoiceRedemption;

public record InvoiceRedemptionRes(Long id,
                                   String sourceInvoiceId,
                                   String sourceInvoiceNumber,
                                   String targetInvoiceId,
                                   String targetInvoiceNumber,
                                   String hostelId,
                                   String hostelName,
                                   Double redemptionAmount,
                                   String referenceNumber,
                                   String reason,
                                   String redeemedAtDate,
                                   String redeemedAtTime,
                                   String createdAtDate,
                                   String createdAtTime,
                                   String createdBy) {
}
