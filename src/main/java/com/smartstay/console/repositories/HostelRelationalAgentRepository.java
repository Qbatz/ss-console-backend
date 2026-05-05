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

    List<HostelRelationalAgent> findAllByHostelIdInOrderByIdDesc(Set<String> hostelIds);

    List<HostelRelationalAgent> findAllByHostelIdOrderByIdDesc(String hostelId);

    @Query("""
                SELECT hra
                FROM HostelRelationalAgent hra
                WHERE hra.agentId = :agentId
                  AND hra.id = (
                      SELECT MAX(h2.id)
                      FROM HostelRelationalAgent h2
                      WHERE h2.hostelId = hra.hostelId
                  )
                ORDER BY hra.id DESC
            """)
    List<HostelRelationalAgent> findLatestByAgentIdPerHostel(@Param("agentId") String agentId);
}
