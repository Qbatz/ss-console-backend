package com.smartstay.console.repositories;

import com.smartstay.console.dao.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {


    Subscription findByHostelId(String hostelId);
}
