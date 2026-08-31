package com.smartstay.console.repositories;

import com.smartstay.console.dao.HostelRelationalAgent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface HostelRelationalAgentRepository extends JpaRepository<HostelRelationalAgent, Long> {

    List<HostelRelationalAgent> findAllByParentIdInOrderByIdDesc(Set<String> parentIds);

    List<HostelRelationalAgent> findAllByParentIdOrderByIdDesc(String parentId);

    @Query(value = """
            SELECT hra.*
            FROM hostel_relational_agent hra
            INNER JOIN (
                SELECT parent_id, MAX(id) AS max_id
                FROM hostel_relational_agent
                GROUP BY parent_id
            ) latest
                ON latest.parent_id = hra.parent_id
               AND latest.max_id = hra.id
            WHERE hra.agent_id = :agentId
            ORDER BY hra.id DESC
            """, nativeQuery = true)
    List<HostelRelationalAgent> findLatestByAgentIdPerOwner(@Param("agentId") String agentId);

    @Query(value = """
            SELECT hra.parent_id
            FROM hostel_relational_agent hra
            INNER JOIN (
                SELECT parent_id, MAX(id) AS max_id
                FROM hostel_relational_agent
                GROUP BY parent_id
            ) latest
                ON latest.parent_id = hra.parent_id
               AND latest.max_id = hra.id
            ORDER BY hra.id DESC
            """, nativeQuery = true)
    Set<String> findAllLatestParentIds();
}
