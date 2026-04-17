package com.smartstay.console.dto.hostel;

import java.util.Date;
import java.util.List;

public record BillingRuleSnapshot(Integer billingRuleId,
                                  Integer billingStartDate,
                                  Integer billDueDays,
                                  Integer noticePeriod,
                                  boolean isInitial,
                                  boolean hasGracePeriod,
                                  Integer gracePeriodDays,
                                  String typeOfBilling,
                                  String billingModel,
                                  boolean shouldNotify,
                                  Date startFrom,
                                  Date endTill,
                                  Date createdAt,
                                  String createdBy,
                                  List<Integer> reminderDays,
                                  String hostelId) {
}
