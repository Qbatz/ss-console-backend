package com.smartstay.console.ennum;

import lombok.Getter;

@Getter
public enum OwnerSortField {

    JOINING_DATE("createdAt", "Joining Date"),
    LATEST_ACTIVITY("latestActivity", "Latest Activity");

    private final String dbField;
    private final String label;

    OwnerSortField(String dbField, String label) {
        this.dbField = dbField;
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