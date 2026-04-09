package com.smartstay.console.repositories;

import com.smartstay.console.dao.CustomerRecurringTracker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRecurringTrackerRepository extends JpaRepository<CustomerRecurringTracker, Long> {
}
