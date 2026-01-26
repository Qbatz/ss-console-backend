package com.smartstay.console.repositories;

import com.smartstay.console.dao.Agent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AgentRepository extends JpaRepository<Agent, String> {
    @Query("""
            SELECT ag FROM Agent ag WHERE ag.agentEmailId=:agentEmailId
            """)
    Agent findByAgentEmailId(String agentEmailId);
    Agent findByAgentId(String userId);
}
