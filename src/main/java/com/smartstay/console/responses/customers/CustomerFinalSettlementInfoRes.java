package com.smartstay.console.responses.customers;

public record CustomerFinalSettlementInfoRes(Double amountToBePaid,
                                             Double totalDeductions,
                                             Double pendingRent,
                                             Double refundableRent,
                                             Double refundableAdvance,
                                             Double ebAmount,
                                             Double unpaidInvoiceAmount,
                                             boolean isRefundable,
                                             String label,
                                             double pendingAmount) {
}
