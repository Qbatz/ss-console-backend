package com.smartstay.console.responses.customers;

import java.util.List;

public record CustomerBookingInfoRes(String label,
                                     Double availableBalance,
                                     Double appliedAmount,
                                     Double paidAmount,
                                     String invoiceNo,
                                     List<RedemptionInfoRes> RedeemedTo) {
}
