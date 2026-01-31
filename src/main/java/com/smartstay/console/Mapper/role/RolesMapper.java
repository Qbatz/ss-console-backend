package com.smartstay.console.Mapper.role;

import com.smartstay.console.dao.AgentRoles;
import com.smartstay.console.dao.RolesPermission;
import com.smartstay.console.ennum.ModuleId;
import com.smartstay.console.responses.roles.Roles;
import com.smartstay.console.responses.roles.RolesPermissionDetails;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RolesMapper implements Function<AgentRoles, Roles> {


    @Override
    public Roles apply(AgentRoles rolesV1) {
        List<RolesPermission> rolePermissions = rolesV1.getPermissions();
        if (rolePermissions == null || rolePermissions.isEmpty()) {
            return new Roles(rolesV1.getRoleId(), rolesV1.getRoleName(), rolesV1.getIsEditable(),  Collections.emptyList());
        }

        List<RolesPermissionDetails> permissionDetails = rolePermissions.stream()
                .map(p -> new RolesPermissionDetails(
                        p.getModuleId(),
                        ModuleId.fromId(p.getModuleId()).name(),
                        p.isCanRead(),
                        p.isCanWrite(),
                        p.isCanDelete(),
                        p.isCanUpdate()
                ))
                .toList();
        return new Roles(rolesV1.getRoleId(), rolesV1.getRoleName(), rolesV1.getIsEditable()!=null && rolesV1.getIsEditable(), permissionDetails);
    }

}
