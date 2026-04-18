package com.smartstay.console.responses.bills;

import java.util.List;

public record BillingRulesResponse(Integer billingRulesId,
                                   Integer billingStartDate,
                                   Integer billDueDays,
                                   Integer noticePeriod,
                                   Integer billingStartDay,
                                   Integer billingEndDay,
                                   String currentPeriodStartDate,
                                   String currentPeriodEndDate,
                                   String lastRecurringDate,
                                   String nextRecurringDate,
                                   boolean isInitial,
                                   boolean hasGracePeriod,
                                   Integer gracePeriodDays,
                                   String typeOfBilling,
                                   String billingModel,
                                   List<Integer> reminderDays,
                                   boolean shouldNotify,
                                   String createdAtDate,
                                   String createdAtTime,
                                   String createdBy) {
}
