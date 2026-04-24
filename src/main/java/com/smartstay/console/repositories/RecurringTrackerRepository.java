package com.smartstay.console.repositories;

import com.smartstay.console.dao.RecurringTracker;
import com.smartstay.console.dto.hostel.InvoiceCountPerTracker;
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

    List<RecurringTracker> findAllByHostelIdOrderByTrackerIdDesc(String hostelId);

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

    @Query(value = """
            SELECT rt.tracker_id AS trackerId, COUNT(i.invoice_id) AS invoiceCount
            FROM recurring_tracker rt
            LEFT JOIN invoicesv1 i
                ON rt.hostel_id = i.hostel_id
                AND i.invoice_mode = 'RECURRING'
                AND DATE(rt.created_at) = DATE(i.created_at)
            WHERE rt.tracker_id IN (:trackerIds)
            GROUP BY rt.tracker_id
            """, nativeQuery = true)
    List<InvoiceCountPerTracker> getGeneratedInvoiceCountPerTracker(@Param("trackerIds") Set<Long> trackerIds);

    RecurringTracker findByHostelIdAndCreationDayAndCreationMonthAndCreationYear(String hostelId, int day, int month, int year);
}
