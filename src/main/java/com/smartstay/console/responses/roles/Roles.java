package com.smartstay.console.responses.roles;

import java.util.List;

public record Roles(Long id, String name, boolean editable, List<RolesPermissionDetails> rolesPermissionDetails) {
}
