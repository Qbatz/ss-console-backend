package com.smartstay.console.responses.hostelRelationalAgent;

public record RelationalAgentResponse(Long id,
                                      String hostelName,
                                      String agentName,
                                      String reason,
                                      String comments,
                                      String createdBy,
                                      String createdAtDate,
                                      String createdAtTime) {
}
