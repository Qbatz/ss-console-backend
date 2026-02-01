package com.smartstay.console.services;


import com.smartstay.console.dao.Subscription;
import com.smartstay.console.repositories.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionService {


    @Autowired
    private SubscriptionRepository subscriptionRepository;


    public Subscription findByHostelId(String hostelId) {
        return subscriptionRepository.findByHostelId(hostelId);
    }
}
