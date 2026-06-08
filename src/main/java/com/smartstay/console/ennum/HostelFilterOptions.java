package com.smartstay.console.ennum;

import lombok.Getter;

@Getter
public enum HostelFilterOptions {

    TOTAL_PROPERTIES("Total Properties"),
    ACTIVE_PROPERTIES("Active Properties"),
    INACTIVE_PROPERTIES("Inactive Properties"),
    USED_TODAY("Used Today"),
    USED_2TO7_DAYS("Used 2-7 days"),
    USED_8TO14_DAYS("Used 8-14 days"),
    USED_15TO30_DAYS("Used 15-30 days"),
    USED_30_DAYS_AGO("Used 30 days ago"),
    NEVER_USED("Never Used"),
    TRIAL_EXPIRING_SOON("Trial Expiring Soon"),;

    private final String label;

    HostelFilterOptions(String label) {
        this.label = label;
    }

    public static HostelFilterOptions get(String value) {
        try {
            return value == null
                    ? TOTAL_PROPERTIES
                    : HostelFilterOptions.valueOf(value);
        } catch (IllegalArgumentException e) {
            return TOTAL_PROPERTIES;
        }
    }
}
