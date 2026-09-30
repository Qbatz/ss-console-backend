package com.smartstay.console.responses.hostelFollowUp;

import java.util.List;

public record HostelFollowUpResponse(Long latestFollowUpId,
                                     String latestStatus,
                                     String latestComments,
                                     String latestReason,
                                     List<HostelFollowUpHistoryRes> hostelFollowUpHistory) {
}
