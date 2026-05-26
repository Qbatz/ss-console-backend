package com.smartstay.console.ennum;

import lombok.Getter;

@Getter
public enum DropReason {
    NO_RESPONSE("No Response"),
    NOT_INTERESTED("Not Interested"),
    DECISION_PENDING("Decision Pending"),
    TEMPORARY_HOLD("Temporary Hold"),
    DUPLICATE_LEAD("Duplicate Lead"),
    FUTURE_FOLLOW_UP("Future Follow-up Required");

    private final String value;

    DropReason(String value) {
        this.value = value;
    }
}
