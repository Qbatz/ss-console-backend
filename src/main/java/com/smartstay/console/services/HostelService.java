package com.smartstay.console.services;


import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.Agent;
import com.smartstay.console.dao.HostelV1;
import com.smartstay.console.dao.Subscription;
import com.smartstay.console.ennum.ModuleId;
import com.smartstay.console.repositories.HostelV1Repository;
import com.smartstay.console.responses.hostel.HostelListResponse;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class HostelService {

    @Autowired
    private HostelV1Repository hostelV1Repository;

    @Autowired
    private AgentRolesService agentRolesService;

    @Autowired
    private AgentService agentService;

    @Autowired
    private Authentication authentication;

    @Autowired
    private SubscriptionService subscriptionService;

    public ResponseEntity<?> fetchAllHostels(Pageable pageable) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.OK);
        }
        String userId = authentication.getName();
        Agent agent = agentService.findUserByUserId(userId);

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Hostels.getId(), Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }


        return new ResponseEntity<>(getAllHostelsWithSubscriptionDetails(pageable), HttpStatus.OK);
    }

    public Page<HostelListResponse> getAllHostelsWithSubscriptionDetails(Pageable pageable) {
        Page<HostelV1> hostels = hostelV1Repository.findAll(pageable);
        return hostels.map(hostel -> {
            String agentName = null;
            if (hostel.getCreatedBy() != null) {
                Agent agent = agentService.findById(hostel.getCreatedBy());
                if (agent != null) {
                    agentName = agent.getFirstName() + " " + agent.getLastName();
                }
            }

            Subscription subscription = subscriptionService.findByHostelId(hostel.getHostelId()); // Or better logic
            // to get active
            // subscription

            Long subId = null;
            String subNum = null;
            String planCode = null;
            String planName = null;
            Date start = null;
            Date end = null;
            Date activated = null;
            Double paid = null;
            Double amount = null;
            Double discount = null;
            Double discountAmt = null;
            Date nextBilling = null;
            Date subCreated = null;
            String subHostelId = null;

            if (subscription != null) {
                subId = subscription.getSubscriptionId();
                subNum = subscription.getSubscriptionNumber();
                subHostelId = subscription.getHostelId();
                planCode = subscription.getPlanCode();
                planName = subscription.getPlanName();
                start = subscription.getPlanStartsAt();
                end = subscription.getPlanEndsAt();
                activated = subscription.getActivatedAt();
                paid = subscription.getPaidAmount();
                amount = subscription.getPlanAmount();
                discount = subscription.getDiscount();
                discountAmt = subscription.getDiscountAmount();
                nextBilling = subscription.getNextBillingAt();
                subCreated = subscription.getCreatedAt();
            }

            return new HostelListResponse(
                    hostel.getHostelName(),
                    agentName,
                    subId,
                    subNum,
                    subHostelId,
                    planCode,
                    planName,
                    start,
                    end,
                    activated,
                    paid,
                    amount,
                    discount,
                    discountAmt,
                    nextBilling,
                    subCreated);
        });
    }
}
