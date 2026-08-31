package com.smartstay.console.ennum;

import lombok.Getter;

@Getter
public enum ProductUpdateAudienceEnum {

    ALL_OWNERS("All owners", "Show to all registered owners"),
    SELECTED_PLANS("Selected subscription plans", "Target by subscription tier"),
    SELECTED_HOSTELS("Selected properties", "Choose specific properties"),
    SELECTED_OWNERS("Selected owners", "Hand-pick individual owners");

    private final String value;
    private final String description;

    ProductUpdateAudienceEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
