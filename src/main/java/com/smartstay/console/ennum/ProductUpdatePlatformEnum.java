package com.smartstay.console.ennum;

import lombok.Getter;

@Getter
public enum ProductUpdatePlatformEnum {

    ANDROID("Android"),
    IOS("ios"),
    WEB("Web");

    private final String value;

    ProductUpdatePlatformEnum(String value) {
        this.value = value;
    }
}
