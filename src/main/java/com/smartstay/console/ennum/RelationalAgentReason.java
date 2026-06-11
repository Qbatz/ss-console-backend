package com.smartstay.console.ennum;

import lombok.Getter;

@Getter
public enum RelationalAgentReason {

    LEAD_GENERATION("Lead generation"),
    DEMO_SCHEDULED("Demo scheduled"),
    DEMO_COMPLETED("Demo completed"),
    CONVERTED_TO_CUSTOMER("Converted to Customer"),
    RENEWAL_FOLLOWUP("Renewal followup"),
    CUSTOMER_SUPPORT("Customer support"),
    ISSUE_RESOLUTION("Issue resolution"),
    ONBOARDING_SUPPORT("Onboarding support"),
    SYSTEM_MIGRATION("System migration"),
    FOLLOW_UP_REQUIRED("Follow up required");

    private final String label;

    RelationalAgentReason(String label) {
        this.label = label;
    }
}
