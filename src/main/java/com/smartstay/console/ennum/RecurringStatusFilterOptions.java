package com.smartstay.console.ennum;

import lombok.Getter;

@Getter
public enum RecurringStatusFilterOptions {

    ALL("All"),
    GENERATED("Generated"),
    NOT_GENERATED("Not Generated");

    private final String label;

    RecurringStatusFilterOptions(String label)
    {
        this.label = label;
    }

    public static RecurringStatusFilterOptions from(String value) {
        try {
            return value == null
                    ? ALL
                    : RecurringStatusFilterOptions.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return ALL;
        }
    }
}
