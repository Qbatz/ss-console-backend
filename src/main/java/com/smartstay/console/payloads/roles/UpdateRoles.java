package com.smartstay.console.payloads.roles;

import java.util.List;

public record UpdateRoles(String roleName,
                          String description,
                          Boolean isActive,
                          List<Permission> permissionList) {
}
