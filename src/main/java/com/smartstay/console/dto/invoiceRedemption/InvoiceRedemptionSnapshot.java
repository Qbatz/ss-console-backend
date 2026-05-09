package com.smartstay.console.dto.invoiceRedemption;

import java.util.Date;

public record InvoiceRedemptionSnapshot(Long id,
                                        String sourceInvoiceId,
                                        String targetInvoiceId,
                                        String hostelId,
                                        Double redemptionAmount,
                                        String referenceNumber,
                                        String transactionId,
                                        String reason,
                                        Date redeemedAt,
                                        String userType,
                                        Boolean isActive,
                                        String createdBy,
                                        String updatedBy,
                                        Date createdAt,
                                        Date updatedAt) {
}
