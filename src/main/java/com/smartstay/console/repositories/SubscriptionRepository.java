package com.smartstay.console.repositories;

import com.smartstay.console.dao.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByHostelId(String hostelId);
    @Query(value = """
            SELECT * FROM subscription WHERE hostel_id=:hostelId order by plan_starts_at DESC LIMIT 1
            """, nativeQuery = true)
    Subscription findLatestSubscription(String hostelId);
}
