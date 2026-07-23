package com.smartstay.console.responses.customers;

public record CustomerFinalSettlementInfoRes(String label,
                                             Double amountToBePaid,
                                             Double fullRent,
                                             Double unpaidInvoiceAmount,
                                             Double otherItemAmount,
                                             Double pendingRent,
                                             Double currentMonthPayableRent,
                                             Double currentMonthPaidRent,
                                             double pendingAmount,
                                             Double totalDeductions,
                                             Double ebAmount,
                                             Double walletAmount,
                                             Double discountAmount,
                                             Double refundableAdvance,
                                             boolean isRefundable,
                                             Double refundableRent) {
}
