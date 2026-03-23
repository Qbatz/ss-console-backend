package com.smartstay.console.repositories;

import com.smartstay.console.dao.RecurringTracker;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface RecurringTrackerRepository extends JpaRepository<RecurringTracker, Long> {

    @Query("""
       SELECT rt
       FROM RecurringTracker rt
       WHERE rt.hostelId IN :hostelIds
       AND rt.trackerId = (
            SELECT MAX(rt2.trackerId)
            FROM RecurringTracker rt2
            WHERE rt2.hostelId = rt.hostelId
       )
       """)
    List<RecurringTracker> getLatestRecurringTrackersByHostelIds(@Param("hostelIds") Set<String> hostelIds);

    boolean existsByHostelIdAndCreationDayAndCreationMonthAndCreationYear(String hostelId, int day, int month, int year);

    Page<RecurringTracker> findAllByHostelIdOrderByTrackerIdDesc(String hostelId, Pageable pageable);

    @Query("""
       SELECT rt
       FROM RecurringTracker rt
       WHERE rt.hostelId = :hostelId
       AND rt.trackerId = (
            SELECT MAX(rt2.trackerId)
            FROM RecurringTracker rt2
            WHERE rt2.hostelId = rt.hostelId
       )
       """)
    RecurringTracker getLatestRecurringTrackerByHostelId(@Param("hostelId") String hostelId);
}
