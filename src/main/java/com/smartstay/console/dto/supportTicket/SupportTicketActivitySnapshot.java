package com.smartstay.console.dto.supportTicket;

import java.util.Date;

public record SupportTicketActivitySnapshot(Long activityId,
                                            String comment,
                                            String description,
                                            String status,
                                            String createdByUserType,
                                            String createdBy,
                                            Date createdAt,
                                            Long ticketId) {
}
