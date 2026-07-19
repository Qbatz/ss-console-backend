package com.smartstay.console.responses.customers;

import java.util.List;

public record CustomerRentInfoRes(Double currentPayableRent,
                                  Double currentRentPaid,
                                  Integer stayDays,
                                  Double currentMonthRent,
                                  Double currentMonthTotalAmount,
                                  Double currentMonthPendingAmount,
                                  String currentInvoiceStartDate,
                                  String currentInvoiceEndDate,
                                  String currentInvoiceId,
                                  Double otherItemAmount,
                                  boolean isDiscountApplied,
                                  Double discountAmount,
                                  Double fullRent,
                                  Double rentDifference,
                                  List<OtherItemsRes> otherItems,
                                  List<RentBreakUpInfoRes> rentBreakUpInfo) {
}
