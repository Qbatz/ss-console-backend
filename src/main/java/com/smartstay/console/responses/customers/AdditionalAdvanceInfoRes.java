package com.smartstay.console.responses.customers;

import java.util.List;

public record AdditionalAdvanceInfoRes(Double totalAmount,
                                       Double paidAmount,
                                       Double advanceBalance,
                                       int totalAdvanceInvoice,
                                       List<AdditionalAdvanceInvoicesRes> advanceInvoices) {
}
