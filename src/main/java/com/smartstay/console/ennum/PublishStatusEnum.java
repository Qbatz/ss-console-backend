package com.smartstay.console.ennum;

import lombok.Getter;

@Getter
public enum PublishStatusEnum {

    DRAFT("Save as draft", "Don't publish yet, continue editing"),
    SCHEDULED("Schedule", "Choose a future date and time"),
    PUBLISHED("Publish now", "Immediately visible to owners"),
    ARCHIVED("Archive", "Product update will be archived");

    private final String value;
    private final String description;

    PublishStatusEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public boolean canTransitionTo(PublishStatusEnum newStatus) {

        if (newStatus == null) {
            return false;
        }

        return switch (this) {
            case DRAFT -> newStatus == DRAFT
                    || newStatus == SCHEDULED
                    || newStatus == PUBLISHED;

            case SCHEDULED -> newStatus == SCHEDULED
                    || newStatus == PUBLISHED;

            case PUBLISHED -> false;

            case ARCHIVED -> false;
        };
    }
}
