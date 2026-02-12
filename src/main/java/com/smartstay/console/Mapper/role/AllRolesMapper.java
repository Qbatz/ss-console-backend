package com.smartstay.console.Mapper.role;

import com.smartstay.console.dao.AgentRoles;
import com.smartstay.console.responses.roles.AllRoles;
import com.smartstay.console.services.AgentService;

import java.util.function.Function;

public class AllRolesMapper implements Function<AgentRoles, AllRoles> {

    private final AgentService agentService;

    public AllRolesMapper(AgentService agentService) {
        this.agentService = agentService;
    }

    @Override
    public AllRoles apply(AgentRoles rolesV1) {
        return new AllRoles(rolesV1.getRoleId(), rolesV1.getRoleName(),
                rolesV1.getIsEditable()!=null && rolesV1.getIsEditable(),
                agentService.findCountOfAgentByRoleId(rolesV1.getRoleId())
        );
    }

}
