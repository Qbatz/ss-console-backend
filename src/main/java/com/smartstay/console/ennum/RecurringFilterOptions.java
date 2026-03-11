package com.smartstay.console.ennum;

import lombok.Getter;

@Getter
public enum RecurringFilterOptions {

    TODAY("today"),
    YESTERDAY("yesterday"),
    TWO_DAYS_AGO("2 days ago"),
    TOMORROW("tomorrow"),
    THIS_WEEK("this week"),
    LAST_WEEK("last week"),
    TILL_TODAY("till today"),
    UP_COMING("up coming");

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
