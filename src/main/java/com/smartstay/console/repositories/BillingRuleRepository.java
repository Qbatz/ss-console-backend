package com.smartstay.console.repositories;

import com.smartstay.console.dao.BillingRules;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.Set;

public interface BillingRuleRepository extends JpaRepository<BillingRules, Integer> {

    @Query(value = """
           SELECT * FROM billing_rules WHERE hostel_id=:hostelId ORDER BY created_at DESC LIMIT 1
           """, nativeQuery = true)
    BillingRules findCurrentBillingRules(@Param("hostelId") String hostelId);

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
            AND b.typeOfBilling = :billingType
            AND (:hostelName IS NULL OR LOWER(b.hostel.hostelName) LIKE LOWER(CONCAT('%', :hostelName, '%')))
            AND (
                r IS NULL OR
                r.creationDay != b.billingStartDate OR
                r.creationMonth != :currentMonth OR
                r.creationYear != :currentYear
            )
            """)
    long countPendingRecurring(@Param("days") Set<Integer> days,
                               @Param("billingType") String billingType,
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
            AND b.typeOfBilling = :billingType
            AND (:hostelName IS NULL OR LOWER(h.hostelName) LIKE LOWER(CONCAT('%', :hostelName, '%')))
            AND (
                hp IS NULL OR
                hp.currentPlanEndsAt IS NULL OR
                hp.currentPlanEndsAt < :now
            )
            """)
    long countExpiredSubscriptions(@Param("days") Set<Integer> days,
                                   @Param("billingType") String billingType,
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
            AND b.typeOfBilling = :billingType
            AND (:hostelName IS NULL OR LOWER(h.hostelName) LIKE LOWER(CONCAT('%', :hostelName, '%')))
            AND (
                :status = 'ALL'
                OR (:status = 'GENERATED' AND r.creationDay = b.billingStartDate)
                OR (:status = 'NOT_GENERATED' AND
                    (r IS NULL OR r.creationDay != b.billingStartDate)
                )
            )
            AND (
                :billingModel = 'ALL'
                OR (b.billingModel = :billingModel)
            )
            """)
    Page<BillingRules> getPaginatedBillingRulesByDays(@Param("days") Set<Integer> days,
                                                      @Param("billingType") String billingType,
                                                      @Param("hostelName") String hostelName,
                                                      @Param("currentMonth") int currentMonth,
                                                      @Param("currentYear") int currentYear,
                                                      @Param("status") String status,
                                                      @Param("billingModel") String billingModel,
                                                      Pageable pageable);
}
