package com.smartstay.console.payloads.invoice;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AdvanceBalanceAmountPayload(@NotNull(message = "Amount is required")
                                          @Positive(message = "Amount should be positive")
                                          double balanceAmount) {
}
