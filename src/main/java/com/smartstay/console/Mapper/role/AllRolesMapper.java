package com.smartstay.console.Mapper.role;

import com.smartstay.console.dao.AgentRoles;
import com.smartstay.console.responses.roles.AllRoles;

import java.util.function.Function;

public class AllRolesMapper implements Function<AgentRoles, AllRoles> {

    long userCount;

    public AllRolesMapper(long userCount) {
        this.userCount = userCount;
    }

    @Override
    public AllRoles apply(AgentRoles rolesV1) {
        return new AllRoles(rolesV1.getRoleId(), rolesV1.getRoleName(),
                rolesV1.getIsEditable()!=null && rolesV1.getIsEditable(),
                userCount
        );
    }

}
