package com.smartstay.console.ennum;

import lombok.Getter;

@Getter
public enum ProductUpdateModuleEnum {

    KYC("Kyc"),
    BILLING("Billing");

    private final String value;

    ProductUpdateModuleEnum(String value) {
        this.value = value;
    }
}
