package com.smartstay.console.responses.supportTicket;

import java.util.List;

public record TicketCurrentStatusResponse(String key,
                                          String label,
                                          List<TicketAllowedStatusResponse> allowedStatuses) {
}
