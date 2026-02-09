package com.smartstay.console.repositories;

import com.smartstay.console.dao.AgentModules;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgentModulesRepository extends JpaRepository<AgentModules, Long> {
    AgentModules findByModuleName(String name);
}
