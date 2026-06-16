package com.smartstay.console.responses.hostelRelationalAgent;

public record HostelRelationalAgentResponse(Long id,
                                            String parentId,
                                            String agentId,
                                            String agentName,
                                            String reason,
                                            String comments,
                                            String createdBy,
                                            String createdAtDate,
                                            String createdAtTime) {
}
