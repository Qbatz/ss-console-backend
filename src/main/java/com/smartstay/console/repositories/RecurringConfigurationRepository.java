package com.smartstay.console.repositories;

import com.smartstay.console.dao.RecurringConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecurringConfigurationRepository extends JpaRepository<RecurringConfiguration, Long> {

    RecurringConfiguration findByHostelId(String hostelId);
}
