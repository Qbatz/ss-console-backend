package com.smartstay.console.dto.kyc;

import java.util.Date;

public record KycHistorySnapshot(Long historyId,
                                 String hostelId,
                                 Date startDate,
                                 Date endDate,
                                 Boolean isCancelledDueToPlan,
                                 String cancellationReason,
                                 String activationReason,
                                 String cancelledBy,
                                 Date createdAt,
                                 String createdBy,
                                 String updatedBy) {
}
