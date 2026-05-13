package com.smartstay.console.dto.agentRoles;

public record RolesPermissionSnapshot(int moduleId,
                                      boolean canRead,
                                      boolean canWrite,
                                      boolean canDelete,
                                      boolean canUpdate) {
}
