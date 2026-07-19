package com.smartstay.console.responses.customers;

import java.util.List;

public record UnpaidInvoicesInfoRes(int unpaidInvoiceCount,
                                    double invoiceTotalAmount,
                                    double paidAmount,
                                    double unpaidAmount,
                                    List<UnpaidInvoicesRes> unpaidInvoices) {
}
