package com.smartstay.console.repositories;

import com.smartstay.console.dao.AgentActivities;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgentActivitiesRepository extends JpaRepository<AgentActivities, Long> {
}
