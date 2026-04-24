package com.smartstay.console.payloads.billingRules;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateBillingRulesPayload(Integer billingStartDate,
                                        Integer billingDueDays,
                                        Integer noticePeriodDays,
                                        Integer gracePeriodDays,
                                        @NotBlank(message = "Type of billing is required")
                                        String typeOfBilling,
                                        @NotBlank(message = "Billing model is required")
                                        String billingModel,
                                        List<@NotNull Integer> reminderDays) {
}
