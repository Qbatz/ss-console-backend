package com.smartstay.console.responses.agents;

import com.smartstay.console.dao.RolesPermission;

import java.util.List;

public record AgentDetails(String fullName,
                           String initials,
                           String emailId,
                           String firstName,
                           String lastName,
                           String mobile,
                           Long roleId,
                           String roleName,
                           List<RolesPermission> permissions) {
}
