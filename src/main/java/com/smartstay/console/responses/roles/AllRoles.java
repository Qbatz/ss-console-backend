package com.smartstay.console.responses.roles;

public record AllRoles(Long id,
                       String name,
                       String description,
                       boolean editable,
                       long userCount,
                       String createdAtDate,
                       String createdAtTime,
                       String updatedAtDate,
                       String updatedAtTime) {
}
