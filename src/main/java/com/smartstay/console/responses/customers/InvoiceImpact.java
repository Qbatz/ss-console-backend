package com.smartstay.console.responses.customers;

import com.smartstay.console.ennum.InvoiceImpactType;

public record InvoiceImpact(String invoiceId,
                            String invoiceNumber,
                            String invoiceType,
                            String invoiceStartDate,
                            String invoiceEndDate,
                            Double totalAmount,
                            Double paidAmount,
                            String paymentStatus,
                            InvoiceImpactType action,
                            String newInvoiceStartDate,
                            String newInvoiceEndDate,
                            boolean paid) {
}