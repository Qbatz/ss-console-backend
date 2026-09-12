package com.smartstay.console.payloads.kyc;

import jakarta.validation.constraints.NotNull;

public record KycMonthLimitPayload(@NotNull(message = "Per month limit is required")
                                   int perMonthLimit) {
}
