package com.smartstay.console.ennum;

import lombok.Getter;

import java.util.Set;

@Getter
public enum RequestStatus {
    PENDING("Pending"),
    REQUESTED("Requested"),
    COMPLETED("Completed"),
    ONBOARDED("Onboarded"),
    ASSIGNED("Assigned"),
    OPEN("Open"),
    ONHOLD("Hold"),
    REJECTED("Rejected"),
    CLOSED("Closed"),
    IN_PROGRESS("In-Progress");

    private final String value;

    RequestStatus(String value) {
        this.value = value;
    }

    public Set<RequestStatus> getAllowedStatuses() {
        return switch (this) {
            case REQUESTED -> Set.of(ASSIGNED, REJECTED);
            case PENDING -> Set.of(ASSIGNED, REJECTED);
            case OPEN -> Set.of(ASSIGNED, REJECTED);
            case ASSIGNED -> Set.of(ASSIGNED, IN_PROGRESS, COMPLETED, ONHOLD, REJECTED);
            case IN_PROGRESS -> Set.of(COMPLETED, ONHOLD, REJECTED);
            case COMPLETED -> Set.of(ONBOARDED);
            case ONBOARDED -> Set.of(CLOSED);
            case ONHOLD -> Set.of(IN_PROGRESS, REJECTED);
            case REJECTED, CLOSED -> Set.of();
        };
    }

    public boolean canMoveTo(RequestStatus nextStatus) {
        return getAllowedStatuses().contains(nextStatus);
    }
}