package com.smartstay.console.responses.customers;

import java.util.List;

public record CustomerBookingInfoRes(String label,
                                     Double paidAmount,
                                     Double availableBalance,
                                     Double appliedAmount,
                                     String invoiceNo,
                                     List<RedemptionInfoRes> RedeemedTo) {
}
