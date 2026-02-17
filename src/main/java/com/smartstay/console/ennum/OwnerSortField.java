package com.smartstay.console.ennum;

import lombok.Getter;

@Getter
public enum OwnerSortField {

    OWNER_NAME("Owner Name"),
    HOSTEL_COUNT("No of Properties"),
    JOINING_DATE("Joining Date"),
    LATEST_ACTIVITY("Latest Activity");

    private final String label;

    OwnerSortField(String label) {
        this.label = label;
    }

    public static OwnerSortField from(String value) {
        try {
            return value == null
                    ? JOINING_DATE
                    : OwnerSortField.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return JOINING_DATE;
        }
    }
}