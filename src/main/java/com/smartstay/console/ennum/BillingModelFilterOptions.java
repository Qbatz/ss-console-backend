package com.smartstay.console.ennum;

import lombok.Getter;

@Getter
public enum BillingModelFilterOptions {

    ALL("All"),
    PREPAID("Prepaid"),
    POSTPAID("Postpaid");

    private final String label;

    BillingModelFilterOptions(String label)
    {
        this.label = label;
    }

    public static BillingModelFilterOptions from(String value) {
        try {
            return value == null
                    ? ALL
                    : BillingModelFilterOptions.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return ALL;
        }
    }
}
