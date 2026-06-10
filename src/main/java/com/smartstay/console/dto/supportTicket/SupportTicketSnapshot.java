package com.smartstay.console.dto.supportTicket;

import java.util.Date;

public record SupportTicketSnapshot(Long ticketId,
                                    String ticketNumber,
                                    String parentId,
                                    String hostelId,
                                    String raisedBy,
                                    String queryType,
                                    String subject,
                                    String priority,
                                    Date issueDate,
                                    String assignedTo,
                                    String assignedBy,
                                    String remarks,
                                    String paymentProof,
                                    String source,
                                    String ticketStatus,
                                    String createdByUserType,
                                    String createdBy,
                                    Date createdAt) {
}
