package com.smartstay.console.ennum;

import lombok.Getter;

@Getter
public enum ProductUpdateTypeEnum {

    NEW_FEATURE("New feature"),
    BUG_FIX("Bug fix"),
    IMPORTANT_UPDATE("Important update"),
    IMPROVEMENT("Improvement");

    private final String value;

    ProductUpdateTypeEnum(String value) {
        this.value = value;
    }
}
