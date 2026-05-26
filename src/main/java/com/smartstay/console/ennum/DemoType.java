package com.smartstay.console.ennum;

import lombok.Getter;

@Getter
public enum DemoType {
    LIVE("Live"),
    ONLINE("Online");

    public final String value;

    DemoType(String value) {
        this.value = value;
    }
}
