package com.smartstay.console.ennum;

import lombok.Getter;

@Getter
public enum DateFilterEnum2 {

    THIS_MONTH("This month"),
    LAST_MONTH("Last month"),
    LAST_6_MONTHS("Last 6 months"),
    CUSTOM("Custom");

    private final String label;

    DateFilterEnum2(String label) {
        this.label = label;
    }
}
