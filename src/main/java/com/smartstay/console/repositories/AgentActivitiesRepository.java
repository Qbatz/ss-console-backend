package com.smartstay.console.repositories;

import com.smartstay.console.dao.AgentActivities;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface AgentActivitiesRepository extends JpaRepository<AgentActivities, Long> {

    @Query(value = """
            SELECT aa.*
            FROM agent_activities aa
            JOIN (
                SELECT agent_id, MAX(created_at) AS created_at
                FROM agent_activities
                WHERE agent_id IN (:agentIds)
                GROUP BY agent_id
            ) latest
            ON latest.agent_id = aa.agent_id
            AND latest.created_at = aa.created_at
            """, nativeQuery = true)
    List<AgentActivities> findLatestActivityByAgentIds(Set<String> agentIds);

    Page<AgentActivities> findAllByAgentIdOrderByCreatedAtDesc(String agentId, Pageable pageable);
}
