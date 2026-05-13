package com.smartstay.console.dto.agentRoles;

import java.util.Date;
import java.util.List;

public record AgentRoleSnapshot(Long roleId,
                                String roleName,
                                Boolean isActive,
                                Boolean isDeleted,
                                Boolean isEditable,
                                Date createdAt,
                                Date updatedAt,
                                List<RolesPermissionSnapshot> permissions) {
}
