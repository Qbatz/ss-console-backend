package com.smartstay.console.repositories;

import com.smartstay.console.dao.AgentActivities;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface AgentActivitiesRepository extends JpaRepository<AgentActivities, Long> {

    @Query("""
            select aa
            from AgentActivities aa
            where aa.agentId in :agentIds
            and aa.createdAt = (
                select max(aa1.createdAt)
                from AgentActivities aa1
                where aa1.agentId = aa.agentId
            )
            """)
    List<AgentActivities> findLatestActivityByAgentIds(Set<String> agentIds);
}
