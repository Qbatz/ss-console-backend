package com.smartstay.console.ennum;

import lombok.Getter;

@Getter
public enum TrialDaysExtReason {
    SALES_FOLLOW_UP("Sales Follow-Up"),
    CUSTOMER_REQUEST("Customer Request"),
    TECHNICAL_DELAY("Technical Delay"),
    INTERNAL_APPROVAL("Internal Approval");

    private final String label;

    TrialDaysExtReason(String label) {
        this.label = label;
    }
}
