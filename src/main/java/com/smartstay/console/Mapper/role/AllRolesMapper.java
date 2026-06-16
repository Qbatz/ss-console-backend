package com.smartstay.console.Mapper.role;

import com.smartstay.console.dao.AgentRoles;
import com.smartstay.console.responses.roles.AllRoles;
import com.smartstay.console.utils.Utils;

import java.util.function.Function;

public class AllRolesMapper implements Function<AgentRoles, AllRoles> {

    long userCount;

    public AllRolesMapper(long userCount) {
        this.userCount = userCount;
    }

    @Override
    public AllRoles apply(AgentRoles roles) {

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

        return new AllRoles(roles.getRoleId(), roles.getRoleName(), roles.getDescription(),
                roles.getIsEditable() != null && roles.getIsEditable(), userCount,
                createdAtDate, createdAtTime, updatedAtDate, updatedAtTime
        );
    }

}
