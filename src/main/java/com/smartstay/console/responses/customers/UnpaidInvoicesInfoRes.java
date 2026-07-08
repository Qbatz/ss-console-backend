package com.smartstay.console.responses.customers;

import java.util.List;

public record UnpaidInvoicesInfoRes(int unpaidInvoiceCount,
                                    double unpaidAmount,
                                    double invoiceTotalAmount,
                                    double paidAmount,
                                    List<UnpaidInvoicesRes> unpaidInvoices) {
}
