package com.smartstay.console.repositories;

import com.smartstay.console.dao.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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

    @Query(value = """
            SELECT * FROM subscription
            WHERE hostel_id = :hostelId
            order by plan_starts_at DESC LIMIT 1
            """, nativeQuery = true)
    Subscription findLatestSubscription(String hostelId);

    @Query("""
            SELECT sc FROM Subscription sc
            WHERE DATE(sc.planStartsAt) = DATE(:date)
            AND sc.isActive=true
            """)
    List<Subscription> findSubscriptionStartingToday(@Param("date") Date date);

    Page<Subscription> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Subscription> findByHostelIdInOrderByCreatedAtDesc(Set<String> hostelIds, Pageable pageable);

    @Query("""
           select count(s)
           from Subscription s
           where s.planEndsAt < CURRENT_TIMESTAMP
           and s.planStartsAt = (
               select max(s2.planStartsAt)
               from Subscription s2
               where s2.hostelId = s.hostelId
           )
           """)
    long getExpiredLatestSubscriptionCount();
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
}
