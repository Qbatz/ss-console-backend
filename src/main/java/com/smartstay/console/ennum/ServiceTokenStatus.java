package com.smartstay.console.ennum;

import lombok.Getter;

@Getter
public enum ServiceTokenStatus {
    ACTIVE("Active"),
    EXPIRING_SOON("Expiring Soon"),
    EXPIRED("Expired");

    private final String label;

    ServiceTokenStatus(String label) {
        this.label = label;
    }
}
