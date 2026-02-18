package com.smartstay.console.responses.bills;

public record BillingRulesResponse(Integer billingRulesId,
                                   Integer billingStartDate,
                                   Integer billDueDays,
                                   Integer noticePeriod) {
}
