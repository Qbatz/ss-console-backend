package com.smartstay.console.ennum;

import lombok.Getter;

@Getter
public enum QueryType {
    GENERAL_QUERY("General Query"),
    FEATURE_REQUEST("Feature Request"),
    COMPLAINT("Complaint"),
    BUG_ISSUE("Bug Issue"),
    REQUIREMENT("Requirement"),
    CLARIFICATION("Clarification");

    private final String label;

    QueryType(String label) {
        this.label = label;
    }
}
