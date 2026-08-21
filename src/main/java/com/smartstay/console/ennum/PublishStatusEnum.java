package com.smartstay.console.ennum;

import lombok.Getter;

@Getter
public enum PublishStatusEnum {

    DRAFT("Save as draft", "Don't publish yet, continue editing"),
    SCHEDULED("Schedule", "Choose a future date and time"),
    PUBLISHED("Publish now", "Immediately visible to owners"),
    ARCHIVED("Archive", "Archive update");

    private final String value;
    private final String description;

    PublishStatusEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
