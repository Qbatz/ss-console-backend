package com.smartstay.console.ennum;

import lombok.Getter;

@Getter
public enum RecurringFilterOptions {

    TODAY("Today"),
    YESTERDAY("Yesterday"),
    TWO_DAYS_AGO("2 days ago"),
    TOMORROW("Tomorrow"),
    THIS_WEEK("This week"),
    LAST_WEEK("Last week"),
    TILL_TODAY("Till today"),
    UP_COMING("Up coming");

    private final String label;

    RecurringFilterOptions(String label)
    {
        this.label = label;
    }

    public static RecurringFilterOptions from(String value) {
        try {
            return value == null
                    ? TODAY
                    : RecurringFilterOptions.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return TODAY;
        }
    }
}
