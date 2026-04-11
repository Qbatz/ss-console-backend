package com.smartstay.console.repositories;

import com.smartstay.console.dao.BillingRules;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface BillingRuleRepository extends JpaRepository<BillingRules, Integer> {

    @Query(value = """
           SELECT * FROM billing_rules WHERE hostel_id=:hostelId ORDER BY created_at DESC LIMIT 1
           """, nativeQuery = true)
    BillingRules findCurrentBillingRules(@Param("hostelId") String hostelId);

    @Query(value = """
            SELECT br.*
            FROM billing_rules br
            INNER JOIN (
                SELECT hostel_id, MAX(created_at) AS max_created_at
                FROM billing_rules
                WHERE hostel_id IN (:hostelIds)
                GROUP BY hostel_id
            ) latest
            ON br.hostel_id = latest.hostel_id
            AND br.created_at = latest.max_created_at
            WHERE br.type_of_billing = :billingType
            """, nativeQuery = true)
    List<BillingRules> findLatestBillingRulesByHostelIdsAndBillingType(@Param("hostelIds") Set<String> hostelIds,
                                                                       @Param("billingType") String billingType);

    @Query("""
            SELECT b
            FROM BillingRules b
            JOIN b.hostel h
            WHERE b.createdAt = (
                SELECT MAX(b2.createdAt)
                FROM BillingRules b2
                WHERE b2.hostel.hostelId = b.hostel.hostelId
            )
            AND b.billingStartDate IN :days
            AND b.typeOfBilling = :billingType
            AND (:hostelIds IS NULL OR h.hostelId IN :hostelIds)
            AND (
                :billingModel = 'ALL'
                OR (b.billingModel = :billingModel)
            )
            """)
    Page<BillingRules> getPaginatedBillingRulesByDays(@Param("days") Set<Integer> days,
                                                      @Param("billingType") String billingType,
                                                      @Param("hostelIds") Set<String> hostelIds,
                                                      @Param("billingModel") String billingModel,
                                                      Pageable pageable);

    @Query("""
            SELECT b
            FROM BillingRules b
            JOIN b.hostel h
            WHERE b.createdAt = (
                SELECT MAX(b2.createdAt)
                FROM BillingRules b2
                WHERE b2.hostel.hostelId = b.hostel.hostelId
            )
            AND b.billingStartDate IN :days
            AND b.typeOfBilling = :billingType
            """)
    List<BillingRules> getLatestBillingRulesByDays(@Param("days") Set<Integer> days,
                                                   @Param("billingType") String billingType);
}
