package com.smartstay.console.payloads.roles;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AddRoles(@NotBlank(message = "Role name is required")
                       String roleName,
                       String description,
                       @NotNull(message = "Permission list can not be null")
                       @NotEmpty(message = "Permission list can not be empty")
                       List<Permission> permissionList) {
}
