package com.smartstay.console.repositories;

import com.smartstay.console.dao.BillingRules;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface BillingRuleRepository extends JpaRepository<BillingRules, Integer> {

    @Query("SELECT b FROM BillingRules b WHERE b.id = :billingRuleId AND b.hostel.id = :hostelId")
    Optional<BillingRules> findBillingRuleByIdAndHostelId(@Param("billingRuleId") Integer billingRuleId,
                                                          @Param("hostelId") String hostelId);

    Optional<BillingRules> findByHostel_hostelId(String hostelId);

    @Query(value = """
            SELECT * FROM billing_rules WHERE (start_from IS NULL OR DATE(start_from) <=DATE(:startDate)) 
            AND hostel_id=:hostelId
            ORDER BY billing_start_date DESC LIMIT 1
            """, nativeQuery = true)
    BillingRules findByHostelIdAndStartDate(@Param("hostelId") String hostelId, @Param("startDate") Date startDate);

    @Query(value = """
            SELECT * FROM billing_rules WHERE DATE(start_from) >=DATE(:startDate) AND hostel_id=:hostelId ORDER BY start_from DESC LIMIT 1
            """, nativeQuery = true)
    BillingRules findNewRuleByHostelIdAndDate(@Param("hostelId") String hostelId, @Param("startDate") Date startDate);

    @Query(value = """
           SELECT * FROM billing_rules WHERE hostel_id=:hostelId ORDER BY created_at DESC LIMIT 1
            """, nativeQuery = true)
    BillingRules findCurrentBillingRules(@Param("hostelId") String hostelId);

    @Query(value = """
            SELECT * FROM billing_rules b WHERE b.billing_start_date =:day
                      AND b.created_at = (
                          SELECT MAX(b2.created_at)
                          FROM billing_rules b2
                          WHERE b2.hostel_id = b.hostel_id
                            group by b2.hostel_id
                      )
            """, nativeQuery = true)
    List<BillingRules> findAllHostelsHavingTodaysRecurring(@Param("day") String day);

    @Query(value = """
            SELECT * FROM billing_rules b WHERE b.billing_start_date =:day
                      AND b.created_at = (
                          SELECT MAX(b2.created_at)
                          FROM billing_rules b2
                          WHERE b2.hostel_id = b.hostel_id
                            group by b2.hostel_id
                      )
            """, nativeQuery = true)
    List<BillingRules> findAllHostelsHavingTodaysRecurring(@Param("day") Integer day);

    @Query("""
            SELECT COUNT(b)
            FROM BillingRules b
            LEFT JOIN RecurringTracker r
                ON b.hostel.hostelId = r.hostelId
                AND r.trackerId = (
                    SELECT MAX(r2.trackerId)
                    FROM RecurringTracker r2
                    WHERE r2.hostelId = r.hostelId
                )
            WHERE b.createdAt = (
                SELECT MAX(b2.createdAt)
                FROM BillingRules b2
                WHERE b2.hostel.hostelId = b.hostel.hostelId
            )
            AND b.billingStartDate IN :days
            AND (:hostelName IS NULL OR LOWER(b.hostel.hostelName) LIKE LOWER(CONCAT('%', :hostelName, '%')))
            AND (
                r IS NULL OR
                r.creationDay != b.billingStartDate OR
                r.creationMonth != :currentMonth OR
                r.creationYear != :currentYear
            )
            """)
    long countPendingRecurring(@Param("days") Set<Integer> days,
                               @Param("hostelName") String hostelName,
                               @Param("currentMonth") int currentMonth,
                               @Param("currentYear") int currentYear);

    @Query("""
            SELECT COUNT(b)
            FROM BillingRules b
            JOIN b.hostel h
            LEFT JOIN h.hostelPlan hp
            WHERE b.createdAt = (
                SELECT MAX(b2.createdAt)
                FROM BillingRules b2
                WHERE b2.hostel.hostelId = b.hostel.hostelId
            )
            AND b.billingStartDate IN :days
            AND (:hostelName IS NULL OR LOWER(h.hostelName) LIKE LOWER(CONCAT('%', :hostelName, '%')))
            AND (
                hp IS NULL OR
                hp.currentPlanEndsAt IS NULL OR
                hp.currentPlanEndsAt < :now
            )
            """)
    long countExpiredSubscriptions(@Param("days") Set<Integer> days,
                                   @Param("hostelName") String hostelName,
                                   @Param("now") Date now);

    @Query("""
            SELECT b
            FROM BillingRules b
            JOIN b.hostel h
            LEFT JOIN RecurringTracker r
                 ON r.trackerId = (
                     SELECT MAX(r2.trackerId)
                     FROM RecurringTracker r2
                     WHERE r2.hostelId = b.hostel.hostelId
                     AND r2.creationMonth = :currentMonth
                     AND r2.creationYear = :currentYear
                 )
            WHERE b.createdAt = (
                SELECT MAX(b2.createdAt)
                FROM BillingRules b2
                WHERE b2.hostel.hostelId = b.hostel.hostelId
            )
            AND b.billingStartDate IN :days
            AND (:hostelName IS NULL OR LOWER(h.hostelName) LIKE LOWER(CONCAT('%', :hostelName, '%')))
            AND (
                :status = 'ALL'
                OR (:status = 'GENERATED' AND r.creationDay = b.billingStartDate)
                OR (:status = 'NOT_GENERATED' AND
                    (r IS NULL OR r.creationDay != b.billingStartDate)
                )
            )
            """)
    Page<BillingRules> getPaginatedBillingRulesByDays(@Param("days") Set<Integer> days,
                                                      @Param("hostelName") String hostelName,
                                                      @Param("currentMonth") int currentMonth,
                                                      @Param("currentYear") int currentYear,
                                                      @Param("status") String status,
                                                      Pageable pageable);
}
