package com.smartstay.console.responses.hostelRelationalAgent;

import com.smartstay.console.responses.hostels.OwnerInfo;

import java.util.List;

public record RelationalAgentResponse(Long id,
                                      String parentId,
                                      OwnerInfo owner,
                                      List<RelationalHostelsResponse> hostels,
                                      String agentId,
                                      String agentName,
                                      String reason,
                                      String comments,
                                      String createdBy,
                                      String createdAtDate,
                                      String createdAtTime) {
}
