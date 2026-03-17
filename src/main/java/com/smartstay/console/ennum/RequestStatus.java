package com.smartstay.console.ennum;

import lombok.Getter;

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
}