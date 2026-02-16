package com.smartstay.console.ennum;

import lombok.Getter;

@Getter
public enum OwnerSortField {
    JOINING_DATE("createdAt"),
    LATEST_ACTIVITY("latestActivity");

    private final String dbField;

    OwnerSortField(String dbField) {
        this.dbField = dbField;
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
