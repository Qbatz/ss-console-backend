package com.smartstay.console.payloads.customers;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;
import java.util.List;

public record CustomerSettlementGeneratePayload(@NotNull(message = "Leaving date is required")
                                                @JsonFormat(pattern = "dd-MM-yyyy")
                                                LocalDate leavingDate,
                                                boolean isCustomRent,
                                                @PositiveOrZero(message = "Custom rent amount must not be less than 0")
                                                double customRentAmount,
                                                List<CusSettlementDeductionsPayload> newDeductions) {
}
