package com.smartstay.console.dto.supportTicket;

import java.util.Date;

public record SupportTicketNotesSnapshot(Long id,
                                         String comment,
                                         String createdByUserType,
                                         String createdBy,
                                         Date createdAt,
                                         Long ticketId) {
}
