package com.smartstay.console.ennum;

import lombok.Getter;

import java.util.Set;

@Getter
public enum TicketStatus {
    WAITING("Waiting", "New support ticket created"),
    ASSIGNED("Assigned", "Assigned ticket to an agent"),
    IN_PROGRESS("In Progress", "Agent is processing the ticket"),
    RESOLVED("Resolved", "Ticket resolved"),
    CLOSED("Closed", "Ticket closed");

    private final String label;
    private final String description;

    TicketStatus(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public Set<TicketStatus> getAllowedStatuses() {
        return switch (this) {
            case WAITING -> Set.of(ASSIGNED);
            case ASSIGNED -> Set.of(ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED);
            case IN_PROGRESS -> Set.of(RESOLVED, CLOSED);
            case RESOLVED -> Set.of();
            case CLOSED -> Set.of();
        };
    }

    public boolean canMoveTo(TicketStatus nextStatus) {
        return getAllowedStatuses().contains(nextStatus);
    }
}
