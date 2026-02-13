package com.smartstay.console.responses.roles;

public record AllRoles(Long id,
                       String name,
                       boolean editable,
                       long userCount) {
}
