package com.smartstay.console.ennum;

import lombok.Getter;

@Getter
public enum DashboardRegionGraphDateFilterEnum {

    LAST_7_DAYS("Last 7 Days"),
    LAST_30_DAYS("Last 30 Days"),
    THIS_MONTH("This Month"),
    THIS_QUARTER("This Quarter");

    private final String value;

    DashboardRegionGraphDateFilterEnum(String value) {
        this.value = value;
    }
}
