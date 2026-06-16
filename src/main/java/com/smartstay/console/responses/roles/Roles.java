package com.smartstay.console.responses.roles;

import java.util.List;

public record Roles(Long id,
                    String name,
                    String description,
                    boolean editable,
                    long userCount,
                    String createdAtDate,
                    String createdAtTime,
                    String updatedAtDate,
                    String updatedAtTime,
                    List<RolesPermissionDetails> rolesPermissionDetails) {
}
