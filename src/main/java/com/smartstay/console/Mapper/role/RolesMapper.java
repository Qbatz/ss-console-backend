package com.smartstay.console.Mapper.role;

import com.smartstay.console.dao.AgentRoles;
import com.smartstay.console.dao.RolesPermission;
import com.smartstay.console.ennum.ModuleId;
import com.smartstay.console.responses.roles.Roles;
import com.smartstay.console.responses.roles.RolesPermissionDetails;
import com.smartstay.console.utils.Utils;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class RolesMapper implements Function<AgentRoles, Roles> {

    long userCount;

    public RolesMapper(long userCount) {
        this.userCount = userCount;
    }

    @Override
    public Roles apply(AgentRoles roles) {

        String createdAtDate = null;
        String createdAtTime = null;
        String updatedAtDate = null;
        String updatedAtTime = null;

        if (roles.getCreatedAt() != null) {
            createdAtDate = Utils.dateToString(roles.getCreatedAt());
            createdAtTime = Utils.dateToTime(roles.getCreatedAt());
        }

        if (roles.getUpdatedAt() != null) {
            updatedAtDate = Utils.dateToString(roles.getUpdatedAt());
            updatedAtTime = Utils.dateToTime(roles.getUpdatedAt());
        }

        List<RolesPermission> rolePermissions = roles.getPermissions();
        if (rolePermissions == null || rolePermissions.isEmpty()) {
            return new Roles(roles.getRoleId(), roles.getRoleName(), roles.getDescription(),
                    roles.getIsEditable(), userCount, createdAtDate, createdAtTime,
                    updatedAtDate, updatedAtTime, Collections.emptyList());
        }

        List<RolesPermissionDetails> permissionDetails = rolePermissions.stream()
                .map(p -> new RolesPermissionDetails(
                        p.getModuleId(),
                        ModuleId.fromId(p.getModuleId()).name(),
                        p.isCanRead(),
                        p.isCanWrite(),
                        p.isCanDelete(),
                        p.isCanUpdate()
                )).toList();

        return new Roles(roles.getRoleId(), roles.getRoleName(), roles.getDescription(),
                roles.getIsEditable() != null && roles.getIsEditable(), userCount,
                createdAtDate, createdAtTime, updatedAtDate, updatedAtTime, permissionDetails);
    }

}
