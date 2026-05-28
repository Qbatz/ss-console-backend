package com.smartstay.console.dto.settlement;

public record AvailableAmountToRedeem(Double availableAdvanceAmountToRedeem,
                                      Double availableBookingAmountToRedeem,
                                      Double totalAvailableAmountToRedeem) {
}
