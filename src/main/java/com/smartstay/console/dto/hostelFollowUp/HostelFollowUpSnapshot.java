package com.smartstay.console.dto.hostelFollowUp;

import java.util.Date;

public record HostelFollowUpSnapshot(Long followUpId,
                                     String hostelId,
                                     String status,
                                     String comments,
                                     String reason,
                                     String createdBy,
                                     Date createdAt) {
}
