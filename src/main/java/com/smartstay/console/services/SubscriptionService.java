package com.smartstay.console.services;

import com.smartstay.console.Mapper.subscription.SubscriptionsResMapper;
import com.smartstay.console.config.Authentication;
import com.smartstay.console.config.FilesConfig;
import com.smartstay.console.config.UploadFileToS3;
import com.smartstay.console.dao.Agent;
import com.smartstay.console.dao.HostelPlan;
import com.smartstay.console.dao.HostelV1;
import com.smartstay.console.dao.Plans;
import com.smartstay.console.ennum.*;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.function.Function;
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
    @Autowired
    private UploadFileToS3 uploadFileToS3;

    public ResponseEntity<?> subscribeHostel(String hostelId, Subscription payload, MultipartFile paymentProof) {

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

        Plans trialPlan = plansService.findTrialPlan();

        String planCode = null;
        String planName = null;
        double paidAmount = 0;
        double planAmount = 0;
        double discountAmount = 0;
        long duration = 30;
        boolean isTrial = false;
        String paymentProofUrl = null;

        if (payload.isTrial()) {
            Plans trialDaysPlan = plansService.findLatestTrialPlan();

            List<com.smartstay.console.dao.Subscription> subscriptions = subscriptionRepository.findByHostelId(hostelId);

            Set<String> trialPlanCodes = new HashSet<>();

            if (trialPlan != null) {
                trialPlanCodes.add(trialPlan.getPlanCode().toLowerCase());
            }
            if (trialDaysPlan != null) {
                trialPlanCodes.add(trialDaysPlan.getPlanCode().toLowerCase());
            }

            if (subscriptions != null) {
                long trialCount = 0;
                long subscriptionCount = 0;

                for (com.smartstay.console.dao.Subscription subscription : subscriptions) {
                    if (trialPlanCodes.contains(subscription.getPlanCode().toLowerCase())) {
                        trialCount++;
                    } else {
                        subscriptionCount++;
                    }
                }

                if (trialCount >= 2) {
                    return new ResponseEntity<>(Utils.TRIAL_EXTENSION_LIMIT_REACHED, HttpStatus.BAD_REQUEST);
                }
                if (subscriptionCount > 0) {
                    return new ResponseEntity<>(Utils.HOSTEL_HAS_SUBSCRIBED_BEFORE, HttpStatus.BAD_REQUEST);
                }
            }

            if (payload.trialDays() > 0){
                if (trialDaysPlan != null) {
                    if (payload.trialDays() > trialDaysPlan.getDuration()){
                        return new ResponseEntity<>(Utils.DAYS_CAN_NOT_BE_HIGHER_THAN_PLAN_DURATION, HttpStatus.BAD_REQUEST);
                    }
                    planCode = trialDaysPlan.getPlanCode();
                    planName = trialDaysPlan.getPlanName();
                    planAmount = trialDaysPlan.getPrice();
                    duration = payload.trialDays();
                }
            } else {
                if (trialPlan != null) {
                    planCode = trialPlan.getPlanCode();
                    planName = trialPlan.getPlanName();
                    planAmount = trialPlan.getPrice();
                    duration = trialPlan.getDuration();
                }
            }

            isTrial = true;
        } else {
            if (payload.planCode() == null) {
                return new ResponseEntity<>(Utils.PLAN_CODE_REQUIRED, HttpStatus.BAD_REQUEST);
            } else {
                Plans plan = plansService.findPlanByPlanCode(payload.planCode());
                if (plan == null) {
                    return new ResponseEntity<>(Utils.INVALID_PLAN_CODE, HttpStatus.BAD_REQUEST);
                }

                if (plan.getPlanType() != null && PlanType.TRIAL.name().equals(plan.getPlanType())) {
                    return new ResponseEntity<>(Utils.TRIAL_PLAN_NOT_ALLOWED, HttpStatus.BAD_REQUEST);
                }

                planCode = payload.planCode();
                planName = plan.getPlanName();
                paidAmount = payload.paidAmount();
                planAmount = plan.getPrice();
                discountAmount = payload.discountAmount();
                duration = plan.getDuration();

                if (paymentProof == null || paymentProof.isEmpty()) {
                    return new ResponseEntity<>(Utils.PAYMENT_PROOF_REQUIRED, HttpStatus.BAD_REQUEST);
                } else {
                    paymentProofUrl = uploadFileToS3.uploadFileToS3(
                            FilesConfig.convertMultipartToFileNew(paymentProof), "subscription/payment-proof");
                }
            }
        }

        if (discountAmount < 0 || discountAmount > planAmount) {
            return new ResponseEntity<>(Utils.INVALID_DISCOUNT, HttpStatus.BAD_REQUEST);
        }

        double discountPercentage = 0;
        if (planAmount > 0) {
            discountPercentage = (discountAmount / planAmount) * 100;
        }

        com.smartstay.console.dao.Subscription newSubscription = new com.smartstay.console.dao.Subscription();
        newSubscription.setSubscriptionNumber(latestSubscription.getSubscriptionNumber());
        newSubscription.setHostelId(hostelId);
        newSubscription.setPlanCode(planCode);
        newSubscription.setPlanName(planName);
        newSubscription.setPlanStartsAt(new Date());
        newSubscription.setPaidAmount(paidAmount);
        newSubscription.setPlanAmount(planAmount);
        newSubscription.setDiscount(discountPercentage);
        newSubscription.setDiscountAmount(discountAmount);
        newSubscription.setCreatedAt(new Date());
        newSubscription.setIsActive(true);
        newSubscription.setPaymentProof(paymentProofUrl);
        newSubscription.setCreatedBy(agent.getAgentId());
        newSubscription.setCreatedByUserType(UserType.AGENT.name());

        if (latestSubscription.getPlanEndsAt() != null) {
            if (Utils.compareWithTwoDates(latestSubscription.getPlanEndsAt(), new Date()) < 0) {
                newSubscription.setPlanStartsAt(new Date());
                Date endDate = Utils.addDaysToDate(new Date(), (int) duration);
                newSubscription.setPlanEndsAt(endDate);
                newSubscription.setNextBillingAt(endDate);
                newSubscription.setActivatedAt(new Date());
            } else {
                Date startDate = Utils.addDaysToDate(latestSubscription.getPlanEndsAt(), 1);
                newSubscription.setPlanStartsAt(startDate);
                Date endDate = Utils.addDaysToDate(startDate, (int) duration);
                newSubscription.setPlanEndsAt(endDate);
                newSubscription.setNextBillingAt(endDate);
                newSubscription.setActivatedAt(new Date());
            }
        }

        newSubscription = subscriptionRepository.save(newSubscription);

        if (latestSubscription.getPlanEndsAt() != null) {
            if (Utils.compareWithTwoDates(latestSubscription.getPlanEndsAt(), new Date()) < 0) {
                HostelPlan hostelPlan = hostelV1.getHostelPlan();
                if (hostelPlan == null) {
                    hostelPlan = new HostelPlan();
                    hostelPlan.setCurrentPlanCode(planCode);
                    hostelPlan.setCurrentPlanName(planName);
                    hostelPlan.setHostel(hostelV1);
                }
                hostelPlan.setCurrentPlanStartsAt(newSubscription.getPlanStartsAt());
                hostelPlan.setCurrentPlanEndsAt(newSubscription.getPlanEndsAt());
                hostelPlan.setCurrentPlanPrice(planAmount);
                hostelPlan.setPaidAmount(paidAmount);
                hostelPlan.setTrial(isTrial);
                hostelPlan.setTrialEndingAt(isTrial ? newSubscription.getPlanEndsAt() : null);

                hostelService.updateHostel(hostelV1);
            }
        }

        agentActivitiesService.createAgentActivity(agent, ActivityType.CREATE, Source.SUBSCRIPTION,
                String.valueOf(newSubscription.getSubscriptionId()), null, newSubscription);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    public List<com.smartstay.console.dto.hostelPlans.HostelPlan> getHostelsActivatingToday() {

        List<com.smartstay.console.dto.hostelPlans.HostelPlan> listHostelPlans = new ArrayList<>();

        Calendar cal = Calendar.getInstance();
//        cal.add(Calendar.DAY_OF_MONTH, 1);

        List<com.smartstay.console.dao.Subscription> listSubscriptions = subscriptionRepository
                .findSubscriptionStartingToday(cal.getTime());
        if (listSubscriptions != null && !listSubscriptions.isEmpty()) {
            listHostelPlans = new ArrayList<>(listSubscriptions
                    .stream()
                    .map(i -> new com.smartstay.console.dto.hostelPlans.HostelPlan(
                            i.getHostelId(),
                            i.getPlanStartsAt(),
                            i.getPlanEndsAt(),
                            i.getPlanCode(),
                            i.getPlanName()))
                    .toList());
        }
        return listHostelPlans;
    }

    public List<com.smartstay.console.dao.Subscription> getSubscriptionsByHostelId(String hostelId){
        return subscriptionRepository.findByHostelId(hostelId);
    }

    public List<com.smartstay.console.dao.Subscription> getSubscriptionsByHostelIds(Set<String> hostelIds){
        return subscriptionRepository.findAllByHostelIdIn(hostelIds);
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

        Set<String> createdByIds = subscriptions.stream()
                .filter(s -> UserType.AGENT.name().equalsIgnoreCase(s.getCreatedByUserType()))
                .map(com.smartstay.console.dao.Subscription::getCreatedBy)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<Agent> agents = agentService.getAgentsByIds(createdByIds);
        Map<String, Agent> agentMap = agents.stream()
                .collect(Collectors.toMap(Agent::getAgentId,
                        Function.identity(), (a, b) -> a));

        List<SubscriptionsResponse> responseList = subscriptions.stream()
                .map(subscription -> {
                    HostelV1 hostel = hostelMap.getOrDefault(subscription.getHostelId(), null);
                    Agent createdByAgent = agentMap.getOrDefault(subscription.getCreatedBy(), null);
                    return new SubscriptionsResMapper(hostel, createdByAgent).apply(subscription);
                }).toList();

        Map<String, Object> response = new HashMap<>();
        response.put("content", responseList);
        response.put("currentPage", page + 1);
        response.put("pageSize", size);
        response.put("totalItems", pagedSubscriptions.getTotalElements());
        response.put("totalPages", pagedSubscriptions.getTotalPages());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public long getExpiredSubscriptionsCount(){
        return subscriptionRepository.getExpiredLatestSubscriptionCount();
    }
}
