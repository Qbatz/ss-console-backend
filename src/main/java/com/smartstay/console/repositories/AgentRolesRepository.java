package com.smartstay.console.repositories;

import com.smartstay.console.dao.AgentRoles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface AgentRolesRepository extends JpaRepository<AgentRoles, Long> {


    AgentRoles findByRoleId(Long roleId);

    @Query(value = """
    SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END
    FROM agent_roles role
    WHERE role.role_name = :roleName
    """, nativeQuery = true)
    int existsByRoleName(@Param("roleName") String roleName);

    @Query(value = """
    SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END
    FROM agent_roles role
    WHERE role.role_name = :roleName and role.role_id != :roleId
    """, nativeQuery = true)
    int existsByRoleNameNotRoleId(@Param("roleName") String roleName,@Param("roleId") long roleId);

    List<AgentRoles> findAllByIsActiveTrueAndIsDeletedFalse();
    AgentRoles findByRoleName(String roleName);

    List<AgentRoles> findAllByRoleIdIn(Set<Long> roleIds);
}
