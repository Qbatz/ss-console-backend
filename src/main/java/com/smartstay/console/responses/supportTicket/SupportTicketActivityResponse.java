package com.smartstay.console.responses.supportTicket;

public record SupportTicketActivityResponse(Long supportTicketActivityId,
                                            String comment,
                                            String description,
                                            String status,
                                            String createdByUserType,
                                            String createdById,
                                            String createdBy,
                                            String createdAtDate,
                                            String createdAtTime) {
}
