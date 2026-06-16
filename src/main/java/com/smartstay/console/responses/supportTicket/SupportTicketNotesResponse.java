package com.smartstay.console.responses.supportTicket;

public record SupportTicketNotesResponse(Long notesId,
                                         String notes,
                                         String createdByUserType,
                                         String createdById,
                                         String createdBy,
                                         String createdAtDate,
                                         String createdAtTime) {
}
