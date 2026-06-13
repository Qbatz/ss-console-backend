package com.smartstay.console.repositories;

import com.smartstay.console.dao.Subscription;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findByHostelId(String hostelId);

    List<Subscription> findAllByHostelIdIn(Set<String> hostelIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Subscription findTopByHostelIdOrderByPlanStartsAtDesc(String hostelId);

    @Query("""
            SELECT sc FROM Subscription sc
            WHERE DATE(sc.planStartsAt) = DATE(:date)
            AND sc.isActive=true
            """)
    List<Subscription> findSubscriptionStartingToday(@Param("date") Date date);

    @Query("""
           select count(s)
           from Subscription s
           where s.hostelId IN :hostelIds
           AND s.planEndsAt < CURRENT_DATE
           AND s.planStartsAt = (
                   SELECT MAX(s2.planStartsAt)
                   FROM Subscription s2
                   WHERE s2.hostelId = s.hostelId
               )
               AND s.subscriptionId = (
                   SELECT MAX(s3.subscriptionId)
                   FROM Subscription s3
                   WHERE s3.hostelId = s.hostelId
                     AND s3.planStartsAt = s.planStartsAt
               )
           ORDER BY s.createdAt DESC
           """)
    long getExpiredLatestSubscriptionCountByHostels(Set<String> hostelIds);

    List<Subscription> findByHostelIdAndPlanCode(String hostelId, String planCode);

    @Query("""
            SELECT sub FROM Subscription sub WHERE sub.hostelId=:hostelId AND
            DATE(sub.planStartsAt) >= DATE(:todaysDate) AND sub.isActive=true
            """)
    List<Subscription> findAnyNewSubscriptionAvailable(String hostelId, Date todaysDate);

    @Query("""
            SELECT sub FROM Subscription sub WHERE sub.hostelId=:hostelId AND
            sub.planCode NOT IN (:planCodes)
            """)
    List<Subscription> findAnyPaidPlanAvailable(String hostelId, List<String> planCodes);

    @Query("""
           SELECT s
           FROM Subscription s
           WHERE s.hostelId IN :hostelIds
               AND s.planStartsAt = (
                   SELECT MAX(s2.planStartsAt)
                   FROM Subscription s2
                   WHERE s2.hostelId = s.hostelId
               )
               AND s.subscriptionId = (
                   SELECT MAX(s3.subscriptionId)
                   FROM Subscription s3
                   WHERE s3.hostelId = s.hostelId
                     AND s3.planStartsAt = s.planStartsAt
               )
           ORDER BY s.createdAt DESC
           """)
    List<Subscription> findLatestSubscriptionsPerHostel(Set<String> hostelIds);

    @Query("""
           SELECT s
           FROM Subscription s
           WHERE s.hostelId IN :hostelIds
             AND s.planCode IN :planCodes
             AND s.planEndsAt >= CURRENT_DATE
             AND s.planStartsAt = (
                 SELECT MAX(s2.planStartsAt)
                 FROM Subscription s2
                 WHERE s2.hostelId = s.hostelId
             )
             AND s.subscriptionId = (
                 SELECT MAX(s3.subscriptionId)
                 FROM Subscription s3
                 WHERE s3.hostelId = s.hostelId
                   AND s3.planStartsAt = s.planStartsAt
             )
           ORDER BY s.createdAt DESC
           """)
    Page<Subscription> findLatestByHostelIdInAndPlanCodeIn(Set<String> hostelIds,
                                                           Set<String> planCodes,
                                                           Pageable pageable);
}
