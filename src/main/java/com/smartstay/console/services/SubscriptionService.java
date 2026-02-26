package com.smartstay.console.services;

import com.smartstay.console.Mapper.subscription.SubscriptionsResMapper;
import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.Agent;
import com.smartstay.console.dao.HostelPlan;
import com.smartstay.console.dao.HostelV1;
import com.smartstay.console.dao.Plans;
import com.smartstay.console.ennum.ActivityType;
import com.smartstay.console.ennum.ModuleId;
import com.smartstay.console.ennum.Source;
import com.smartstay.console.payloads.subscription.Subscription;
import com.smartstay.console.repositories.SubscriptionRepository;
import com.smartstay.console.responses.subscriptions.SubscriptionsResponse;
import com.smartstay.console.utils.Constants;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SubscriptionService {
    @Autowired
    private Authentication authentication;
    @Autowired
    private AgentService agentService;
    @Autowired
    private AgentRolesService agentRolesService;
    @Autowired
    private HostelService hostelService;
    @Autowired
    private PlansService plansService;
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private AgentActivitiesService agentActivitiesService;

    public ResponseEntity<?> subscribeHostel(String hostelId, Subscription subscription) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Constants.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Constants.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Subscriptions.getId(), Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        HostelV1 hostelV1 = hostelService.getHostelInfo(hostelId);
        if (hostelV1 == null) {
            return new ResponseEntity<>(Utils.INVALID_HOSTEL_ID, HttpStatus.BAD_REQUEST);
        }

        com.smartstay.console.dao.Subscription latestSubscription = subscriptionRepository.findLatestSubscription(hostelId);
        if (latestSubscription == null) {
            return new ResponseEntity<>(Utils.INVALID_SUBSCRIPTION, HttpStatus.BAD_REQUEST);
        }

        Plans plans = plansService.findTrialPlan(hostelId);
        String planCode = null;
        String planName = null;
        if (subscription == null) {
            if (!latestSubscription.getPlanCode().equalsIgnoreCase(plans.getPlanCode())) {
                return new ResponseEntity<>(Utils.PLAN_CODE_REQUIRED, HttpStatus.BAD_REQUEST);
            }
            else {
                planCode = plans.getPlanCode();
                planName = plans.getPlanName();
            }
        }
        else {
            if (subscription.planCode() == null) {
                if (!latestSubscription.getPlanCode().equalsIgnoreCase(plans.getPlanCode())) {
                    return new ResponseEntity<>(Utils.PLAN_CODE_REQUIRED, HttpStatus.BAD_REQUEST);
                }
                else {
                    planCode = plans.getPlanCode();
                    planName = plans.getPlanName();
                }
            }
            else {
                Plans pla = plansService.findPlanByPlanCode(subscription.planCode());
                if (pla == null) {
                    return new ResponseEntity<>(Utils.INVALID_PLAN_CODE, HttpStatus.BAD_REQUEST);
                }
                planCode = subscription.planCode();
                planName = pla.getPlanName();
            }
        }

        com.smartstay.console.dao.Subscription newSubscription = new com.smartstay.console.dao.Subscription();
        newSubscription.setSubscriptionNumber(latestSubscription.getSubscriptionNumber());
        newSubscription.setHostelId(hostelId);
        newSubscription.setPlanCode(planCode);
        newSubscription.setPlanName(planName);
        newSubscription.setPlanStartsAt(new Date());
        newSubscription.setPaidAmount(0.0);
        newSubscription.setPlanAmount(0.0);
        newSubscription.setDiscount(0.0);
        newSubscription.setDiscountAmount(0.0);
        newSubscription.setCreatedAt(new Date());
        newSubscription.setIsActive(true);

        if (latestSubscription.getPlanEndsAt() != null) {
            if (Utils.compareWithTwoDates(latestSubscription.getPlanEndsAt(), new Date()) < 0) {
                newSubscription.setPlanStartsAt(new Date());
                Date endDate = Utils.addDaysToDate(new Date(), 30);
                newSubscription.setPlanEndsAt(endDate);
                newSubscription.setNextBillingAt(endDate);
                newSubscription.setActivatedAt(endDate);
            }
            else {
                Date startDate = Utils.addDaysToDate(latestSubscription.getPlanEndsAt(), 1);
                newSubscription.setPlanStartsAt(startDate);
                Date endDate = Utils.addDaysToDate(startDate, 30);
                newSubscription.setPlanEndsAt(endDate);
                newSubscription.setNextBillingAt(endDate);
                newSubscription.setActivatedAt(new Date());
            }
        }


        if (Utils.compareWithTwoDates(latestSubscription.getPlanEndsAt(), new Date()) < 0) {
            HostelPlan hostelPlan = hostelV1.getHostelPlan();
            if (hostelPlan == null) {
                hostelPlan = new HostelPlan();
                hostelPlan.setCurrentPlanCode(latestSubscription.getPlanCode());
                hostelPlan.setCurrentPlanName(latestSubscription.getPlanName());
                hostelPlan.setHostel(hostelV1);
            }
            hostelPlan.setCurrentPlanStartsAt(newSubscription.getPlanStartsAt());
            hostelPlan.setCurrentPlanEndsAt(newSubscription.getPlanEndsAt());
            hostelPlan.setCurrentPlanPrice(0.0);
            hostelPlan.setPaidAmount(0.0);
            hostelPlan.setTrial(true);
            hostelPlan.setTrialEndingAt(newSubscription.getPlanEndsAt());

            hostelService.updateHostel(hostelV1);
        }


        newSubscription = subscriptionRepository.save(newSubscription);

        agentActivitiesService.createAgentActivity(agent, ActivityType.CREATE, Source.SUBSCRIPTION,
                String.valueOf(newSubscription.getSubscriptionId()), null, newSubscription);

        return new ResponseEntity<>(HttpStatus.OK);

    }

    public List<com.smartstay.console.dao.Subscription> getAllSubscriptions(List<String> hostelIds) {
        return null;
    }

    public List<com.smartstay.console.dto.hostelPlans.HostelPlan> getHostelsActivatingToday() {
        List<com.smartstay.console.dto.hostelPlans.HostelPlan> listHostelPlans = new ArrayList<>();
        List<com.smartstay.console.dao.Subscription> listSubscriptions = subscriptionRepository.findSubscriptionStartingToday(new Date());
        if (listSubscriptions != null && !listSubscriptions.isEmpty()) {
            listHostelPlans = new ArrayList<>(listSubscriptions
                    .stream()
                    .map(i -> new com.smartstay.console.dto.hostelPlans.HostelPlan(i.getHostelId(),  i.getPlanStartsAt(),
                            i.getPlanEndsAt(),
                            i.getPlanCode(),
                            i.getPlanCode())).toList());
        }
        return listHostelPlans;
    }

    public List<com.smartstay.console.dao.Subscription> getSubscriptionsByHostelId(String hostelId){
        return subscriptionRepository.findByHostelId(hostelId);
    }

    public ResponseEntity<?> getSubscriptions(int page, int size, String hostelName) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Constants.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Constants.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!agentRolesService.checkPermission(agent.getRoleId(), ModuleId.Subscriptions.getId(), Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        page = Math.max(page - 1, 0);
        size = Math.max(size, 1);

        Pageable pageable = PageRequest.of(page, size);

        Page<com.smartstay.console.dao.Subscription> pagedSubscriptions;
        Map<String, HostelV1> hostelMap;
        List<com.smartstay.console.dao.Subscription> subscriptions;

        if (hostelName != null && !hostelName.isBlank()) {

            List<HostelV1> filteredHostels =
                    hostelService.getHostelsByHostelName(hostelName);

            Set<String> filteredHostelIds = filteredHostels.stream()
                    .map(HostelV1::getHostelId)
                    .collect(Collectors.toSet());

            if (filteredHostelIds.isEmpty()) {
                Map<String, Object> emptyResponse = new HashMap<>();
                emptyResponse.put("content", List.of());
                emptyResponse.put("currentPage", page + 1);
                emptyResponse.put("pageSize", size);
                emptyResponse.put("totalItems", 0);
                emptyResponse.put("totalPages", 0);

                return new ResponseEntity<>(emptyResponse, HttpStatus.OK);
            } else {
                pagedSubscriptions = subscriptionRepository
                        .findByHostelIdInOrderByCreatedAtDesc(filteredHostelIds, pageable);
            }

            subscriptions = pagedSubscriptions.getContent();

            hostelMap = filteredHostels.stream()
                    .collect(Collectors.toMap(HostelV1::getHostelId,
                            hostel -> hostel));

        } else {
            pagedSubscriptions = subscriptionRepository
                    .findAllByOrderByCreatedAtDesc(pageable);

            subscriptions = pagedSubscriptions.getContent();

            Set<String> hostelIds = subscriptions.stream()
                    .map(com.smartstay.console.dao.Subscription::getHostelId)
                    .collect(Collectors.toSet());

            List<HostelV1> hostels = hostelService.getHostelsByHostelIds(hostelIds);

            hostelMap = hostels.stream()
                    .collect(Collectors.toMap(HostelV1::getHostelId,
                            hostel -> hostel));
        }

        List<SubscriptionsResponse> responseList = subscriptions.stream()
                .map(subscription -> new SubscriptionsResMapper(
                        hostelMap.getOrDefault(subscription.getHostelId(), null)
                ).apply(subscription)).toList();

        Map<String, Object> response = new HashMap<>();
        response.put("content", responseList);
        response.put("currentPage", page + 1);
        response.put("pageSize", size);
        response.put("totalItems", pagedSubscriptions.getTotalElements());
        response.put("totalPages", pagedSubscriptions.getTotalPages());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
