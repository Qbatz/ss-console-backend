package com.smartstay.console.dao.hostelRelationalAgent;

import com.smartstay.console.ennum.RelationalAgentReason;

import java.util.Date;

public record HostelRelationalAgentSnapshot(Long id,
                                            String hostelId,
                                            String agentId,
                                            RelationalAgentReason reason,
                                            String comments,
                                            String createdBy,
                                            Date createdAt) {
}
