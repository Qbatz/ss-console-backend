package com.smartstay.console.payloads.subscription;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record Subscription(Integer trialDays,
                           @NotNull(message = "Plan code is required")
                           @NotEmpty(message = "Plan code is required")
                           String planCode,
                           Double paidAmount,
                           Double discountAmount,
                           String paidBy,
                           @JsonFormat(pattern = "dd-MM-yyyy")
                           LocalDate paidAtDate,
                           @JsonFormat(pattern = "HH:mm")
                           LocalTime paidAtTime,
                           String trialDaysReason,
                           String trialDaysRemarks) {
}
