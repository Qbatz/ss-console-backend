package com.smartstay.console.ennum;

import lombok.Getter;

@Getter
public enum Priority {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High");

    private final String label;

    Priority(String label) {
        this.label = label;
    }
}
