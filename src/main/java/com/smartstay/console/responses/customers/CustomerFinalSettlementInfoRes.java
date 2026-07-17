package com.smartstay.console.responses.customers;

public record CustomerFinalSettlementInfoRes(Double amountToBePaid,
                                             Double unpaidInvoiceAmount,
                                             double pendingAmount,
                                             Double pendingRent,
                                             Double currentMonthPaidRent,
                                             Double totalDeductions,
                                             Double ebAmount,
                                             Double walletAmount,
                                             Double discountAmount,
                                             Double refundableAdvance,
                                             boolean isRefundable,
                                             Double refundableRent,
                                             String label
                                             ) {
}
