package com.smartstay.console.payloads.billingRules;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateBillingRulesPayload(Integer billingStartDate,
                                        Integer billingDueDays,
                                        Integer noticePeriodDays,
                                        Integer gracePeriodDays,
                                        String typeOfBilling,
                                        String billingModel,
                                        List<@NotNull Integer> reminderDays,
                                        boolean shouldDeleteInvoices) {
}
