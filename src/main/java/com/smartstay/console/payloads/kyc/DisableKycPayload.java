package com.smartstay.console.payloads.kyc;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public record DisableKycPayload(@JsonFormat(pattern = "dd-MM-yyyy")
                                LocalDate endDate,
                                boolean cancelledDueToPlan,
                                String cancellationReason) {
}
