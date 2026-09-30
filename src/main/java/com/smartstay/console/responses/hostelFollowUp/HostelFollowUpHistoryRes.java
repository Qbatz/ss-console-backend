package com.smartstay.console.responses.hostelFollowUp;

public record HostelFollowUpHistoryRes(Long followUpId,
                                       String status,
                                       String comments,
                                       String reason,
                                       String createdById,
                                       String createdBy,
                                       String createdAtDate,
                                       String createdAtTime) {
}
