package com.smartstay.console.repositories;

import com.smartstay.console.dao.Agent;
import com.smartstay.console.dto.agent.RoleCountProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface AgentRepository extends JpaRepository<Agent, String> {

    @Query("""
            SELECT ag FROM Agent ag WHERE ag.agentEmailId=:agentEmailId
            AND ag.isActive = true
            """)
    Agent findByAgentEmailId(String agentEmailId);

    Agent findByAgentId(String agentId);

    Agent findByAgentIdAndIsActiveFalse(String agentId);

    Agent findByAgentIdAndIsActiveTrue(String agentId);

    List<Agent> findByRoleIdAndIsActiveTrue(long roleId);

    @Query("""
       SELECT a.roleId AS roleId, COUNT(a) AS count
       FROM Agent a
       WHERE a.roleId IN :roleIds
         AND a.isActive = true
       GROUP BY a.roleId
       """)
    List<RoleCountProjection> countActiveAgentsByRoleIds(@Param("roleIds") List<Long> roleIds);

    List<Agent> findAllByIsMockAgentFalseAndAgentIdNotOrderByCreatedAtDesc(String agentId);

    List<Agent> findAllByAgentIdInAndIsActiveTrue(Set<String> agentIds);

    List<Agent> findAllByAgentIdIn(Set<String> agentIds);

    boolean existsByAgentEmailIdAndIsMockAgentFalse(String email);

    boolean existsByAgentEmailId(String email);

    @Query("""
            select count(a)
            from Agent a
            where a.isActive = true
            and a.isMockAgent = false
            """)
    long getCount();

    List<Agent> findAllByIsMockAgentFalseAndIsActiveTrueOrderByCreatedAtDesc();
}
