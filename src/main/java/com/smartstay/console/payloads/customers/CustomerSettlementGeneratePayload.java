package com.smartstay.console.payloads.customers;

import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

public record CustomerSettlementGeneratePayload(boolean isCustomRent,
                                                @PositiveOrZero(message = "Custom rent amount must not be less than 0")
                                                double customRentAmount,
                                                List<CusSettlementDeductionsPayload> newDeductions) {
}
