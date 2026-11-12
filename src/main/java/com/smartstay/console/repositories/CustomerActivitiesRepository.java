package com.smartstay.console.repositories;

import com.smartstay.console.dao.CustomerActivities;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerActivitiesRepository extends JpaRepository<CustomerActivities, Long> {

    Page<CustomerActivities> findAllByCustomerIdOrderByCreatedAtDesc(String customerId, Pageable pageable);
}
