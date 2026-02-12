package com.smartstay.console.repositories;

import com.smartstay.console.dao.Agent;
import com.smartstay.console.dto.agent.RoleCountProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgentRepository extends JpaRepository<Agent, String> {
    @Query("""
            SELECT ag FROM Agent ag WHERE ag.agentEmailId=:agentEmailId
            """)
    Agent findByAgentEmailId(String agentEmailId);
    Agent findByAgentId(String userId);
    Agent findByAgentIdAndIsActiveTrue(String userId);
    List<Agent> findByRoleIdAndIsActiveTrue(long roleId);
    @Query("""
       SELECT a.roleId AS roleId, COUNT(a) AS count
       FROM Agent a
       WHERE a.roleId IN :roleIds
         AND a.isActive = true
       GROUP BY a.roleId
       """)
    List<RoleCountProjection> countActiveAgentsByRoleIds(@Param("roleIds") List<Long> roleIds);

}
