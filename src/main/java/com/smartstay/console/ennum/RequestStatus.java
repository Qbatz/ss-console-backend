package com.smartstay.console.ennum;

public enum RequestStatus {
    PENDING("Pending"),
    REQUESTED("REQUESTED"),
    ASSIGNED("Assigned"),
    OPEN("Open"),
    ONHOLD("Hold"),
    REJECTED("Rejected"),
    CLOSED("Closed"),
    INPROGRESS("In-Progress");


    RequestStatus(String pending) {
    }
}
