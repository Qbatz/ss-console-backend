package com.smartstay.console.repositories;

import com.smartstay.console.dao.CustomerRecurringTracker;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface CustomerRecurringTrackerRepository extends JpaRepository<CustomerRecurringTracker, Long> {

    @Query("""
       SELECT rt
       FROM CustomerRecurringTracker rt
       WHERE rt.customerId IN :customerIds
       AND rt.trackerId = (
            SELECT MAX(rt2.trackerId)
            FROM CustomerRecurringTracker rt2
            WHERE rt2.customerId = rt.customerId
       )
       """)
    List<CustomerRecurringTracker> findLatestRecurringTrackerByCustomerIds(@Param("customerIds") Set<String> customerIds);

    CustomerRecurringTracker findTopByCustomerIdOrderByTrackerIdDesc(String customerId);

    boolean existsByCustomerIdAndCreationDayAndCreationMonthAndCreationYear(String customerId, int day, int month, int year);

    Page<CustomerRecurringTracker> findAllByCustomerIdOrderByTrackerIdDesc(String customerId, Pageable pageable);

    List<CustomerRecurringTracker> findAllByHostelIdOrderByTrackerIdDesc(String hostelId);

    CustomerRecurringTracker findTopByHostelIdOrderByTrackerIdDesc(String hostelId);
}
