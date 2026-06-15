package com.smartstay.console.repositories;

import com.smartstay.console.dao.TrialDaysExtension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrialDaysExtensionRepository extends JpaRepository<TrialDaysExtension, Long> {
}
