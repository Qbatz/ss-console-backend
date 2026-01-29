package com.smartstay.console.payloads.roles;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateRoles(
        String roleName,
        Boolean isActive,
        @NotNull(message = "Permission list cannot be null")
        @Size(min = 1, max = 15, message = "Permission list must contain between 1 and 15 items")
        List<Permission> permissionList
) {
}
