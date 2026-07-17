package com.smartstay.console.responses.customers;

public record CustomerFinalSettlementInfoRes(String label,
                                             Double amountToBePaid,
                                             Double fullRent,
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
                                             Double refundableRent
                                             ) {
}
