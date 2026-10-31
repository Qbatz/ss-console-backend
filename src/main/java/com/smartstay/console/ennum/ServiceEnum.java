package com.smartstay.console.ennum;

import lombok.Getter;

@Getter
public enum ServiceEnum {

    zoho("Zoho"),
    SS_REPORTS("Reports"),
    SS_PAYMENTS("Payments");

    private final String label;

    ServiceEnum(String label) {
        this.label = label;
    }
}
