package com.smartstay.console.ennum;

import lombok.Getter;

@Getter
public enum HostelOnboardedDateFilterEnum {

    WEEK("Week"),
    MONTH("Month"),
    QUARTER("Quarter"),
    SIX_MONTHS("6 Months");

    private final String value;

    HostelOnboardedDateFilterEnum(String value) {
        this.value = value;
    }
}
