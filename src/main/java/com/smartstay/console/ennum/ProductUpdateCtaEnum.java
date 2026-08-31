package com.smartstay.console.ennum;

import lombok.Getter;

@Getter
public enum ProductUpdateCtaEnum {

    LEARN_MORE("Learn more"),
    CHECK_IT_OUT("Check it out"),
    NO_CTA("No cta");

    private final String value;

    ProductUpdateCtaEnum(String value) {
        this.value = value;
    }
}
