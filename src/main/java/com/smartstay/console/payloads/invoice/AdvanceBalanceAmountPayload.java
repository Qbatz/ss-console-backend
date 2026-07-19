package com.smartstay.console.payloads.invoice;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record AdvanceBalanceAmountPayload(@NotNull(message = "Amount is required")
                                          @PositiveOrZero(message = "Amount should be positive")
                                          double balanceAmount) {
}
