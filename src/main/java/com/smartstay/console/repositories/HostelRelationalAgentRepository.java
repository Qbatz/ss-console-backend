package com.smartstay.console.repositories;

import com.smartstay.console.dao.HostelRelationalAgent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface HostelRelationalAgentRepository extends JpaRepository<HostelRelationalAgent, Long> {

    List<HostelRelationalAgent> findAllByHostelIdInOrderByIdDesc(Set<String> hostelIds);

    List<HostelRelationalAgent> findAllByHostelIdOrderByIdDesc(String hostelId);

    List<HostelRelationalAgent> findAllByAgentIdOrderByIdDesc(String agentId);
}
