package com.smartstay.console.responses.customers;

import java.util.List;

public record InvoiceDeductionsRes(String invoiceId,
                                   String invoiceNumber,
                                   String invoiceType,
                                   List<DeductionsRes> invoiceAdvanceDeductions) {
}
