package com.smartstay.console.ennum;

import lombok.Getter;

@Getter
public enum DateFilterEnum {

    TODAY("Today"),
    THIS_WEEK("This week"),
    THIS_MONTH("This month"),
    LAST_MONTH("Last month"),
    LAST_3_MONTHS("Last 3 months"),
    LAST_6_MONTHS("Last 6 months"),
    CUSTOM("Custom");

    private final String value;

    DateFilterEnum(String value) {
        this.value = value;
    }
}
