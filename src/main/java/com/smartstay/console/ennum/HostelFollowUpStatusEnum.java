package com.smartstay.console.ennum;

import lombok.Getter;

import java.util.List;

@Getter
public enum HostelFollowUpStatusEnum {

    ACTIVE("Active", List.of("Trial Active", "Paid Active", "Follow-Up Ongoing")),
    ON_HOLD("On hold", List.of("Decision Pending", "Owner Unavailable", "Hostel Not Started",
            "Future Requirement", "Budget Discussion", "Internal Approval Pending", "Follow-Up Later")),
    NOT_INTERESTED("Not interested", List.of("Budget Issue", "Using Another Software",
            "Requirement Not Matching", "Technical Concern", "Feature Not Available")),
    DROPPED("Dropped", List.of("No Response", "Call Not Picked", "Trial Expired",
            "Follow-Up Delayed", "Subscription Cancelled", "Payment Issue", "Switched To Another Software",
            "Property Closed", "Invalid Contact"));

    private final String value;
    private final List<String> reasons;

    HostelFollowUpStatusEnum(String value, List<String> reasons) {
        this.value = value;
        this.reasons = reasons;
    }
}
