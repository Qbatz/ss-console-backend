package com.smartstay.console.dto.settlementDetails;

import java.util.Date;

public record SettlementDetailsSnapshot(Long id,
                                        String customerId,
                                        Date leavingDate,
                                        Date createdAt,
                                        Date updatedAt,
                                        String createdBy,
                                        String updatedBy) {
}
