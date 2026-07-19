package com.smartstay.console.responses.customers;

public record AvailableRedemptionAmountRes(Double availableBookingAmountToRedeem,
                                           Double availableAdvanceAmountToRedeem,
                                           Double totalAvailableAmountToRedeem) {
}
