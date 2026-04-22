package com.smartstay.console.services;

import com.smartstay.console.Mapper.subscription.SubscriptionsResMapper;
import com.smartstay.console.config.Authentication;
import com.smartstay.console.config.FilesConfig;
import com.smartstay.console.config.UploadFileToS3;
import com.smartstay.console.dao.*;
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
    private OrderHistoryService orderHistoryService;
    @Autowired
    private UploadFileToS3 uploadFileToS3;
    @Autowired
    private UsersService usersService;

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

        if (payload == null) {
            return new ResponseEntity<>(Utils.PAYLOAD_REQUIRED, HttpStatus.BAD_REQUEST);
        }

        if (payload.paidBy() != null && !payload.paidBy().isBlank()){
            Users users = usersService.getUserById(payload.paidBy());
            if (users == null){
                return new ResponseEntity<>(Utils.USER_NOT_FOUND, HttpStatus.BAD_REQUEST);
            }
            if (!hostelV1.getParentId().equals(users.getParentId())){
                return new ResponseEntity<>(Utils.PAID_BY_HOSTEL_MISMATCH, HttpStatus.BAD_REQUEST);
            }
        }

        boolean isTrial = false;
        double paidAmount = 0.0;
        double discountAmount = 0.0;
        double discountPercentage = 100.0;
        String paymentProofUrl = null;
        int duration = 1;
        Date today = new Date();

        Plans plans = plansService.findPlanByPlanCode(payload.planCode());

        if (plans == null) {
            return new ResponseEntity<>(Utils.INVALID_PLAN_CODE, HttpStatus.BAD_REQUEST);
        }

        if (plans.getDuration().intValue() <= 0) {
            return new ResponseEntity<>(Utils.INVALID_PLAN_DURATION, HttpStatus.BAD_REQUEST);
        }

        com.smartstay.console.dao.Subscription newSubscription = new com.smartstay.console.dao.Subscription();

        if (plans.getPlanType().equalsIgnoreCase(PlanType.TRIAL.name())) {

            isTrial = true;

            List<com.smartstay.console.dao.Subscription> hostelSubscriptions = subscriptionRepository
                    .findByHostelIdAndPlanCode(hostelId, payload.planCode());

            if (hostelSubscriptions.size() > 1) {
                return new ResponseEntity<>(Utils.TRIAL_EXTENSION_LIMIT_REACHED, HttpStatus.BAD_REQUEST);
            }

            List<com.smartstay.console.dao.Subscription> newSubscriptionForHostel = subscriptionRepository
                    .findAnyNewSubscriptionAvailable(hostelId, today);

            if (!newSubscriptionForHostel.isEmpty()) {
                return new ResponseEntity<>(Utils.NEW_SUBSCRIPTION_IS_ADDED, HttpStatus.BAD_REQUEST);
            }

            duration = plans.getDuration().intValue();
        }
        else if (plans.getPlanType().equalsIgnoreCase(PlanType.EXPANDABLE_TRIAL.name())) {

            isTrial = true;

            int freeTrialDays = Utils.DEFAULT_EXPANDABLE_TRIAL_DAYS;

            if (payload.trialDays() != null) {
                try {
                    freeTrialDays = Integer.parseInt(payload.trialDays().toString());
                }
                catch (Exception e) {
                    freeTrialDays = Utils.DEFAULT_EXPANDABLE_TRIAL_DAYS;
                }
            }

            int maxTrialDays = plans.getDuration().intValue();

            if (freeTrialDays < 1 || freeTrialDays > maxTrialDays) {
                return new ResponseEntity<>(Utils.INVALID_TRIAL_DAYS, HttpStatus.BAD_REQUEST);
            }

            List<Plans> freePlans = plansService.getFreePlans();
            List<String> freePlanCodes = freePlans.stream()
                    .map(Plans::getPlanCode)
                    .toList();

            List<com.smartstay.console.dao.Subscription> listPaidSubscriptions = subscriptionRepository
                    .findAnyPaidPlanAvailable(hostelId, freePlanCodes);

            if (listPaidSubscriptions != null && !listPaidSubscriptions.isEmpty()) {
                return new ResponseEntity<>(Utils.CANNOT_EXTEND_FREE_TRIAL_ANY_MORE, HttpStatus.BAD_REQUEST);
            }

            List<com.smartstay.console.dao.Subscription> newSubscriptionForHostel = subscriptionRepository
                    .findAnyNewSubscriptionAvailable(hostelId, today);

            if (!newSubscriptionForHostel.isEmpty()) {
                return new ResponseEntity<>(Utils.NEW_SUBSCRIPTION_IS_ADDED, HttpStatus.BAD_REQUEST);
            }

            duration = freeTrialDays;
        }
        else {

            if (payload.paidAmount() == null) {
                return new ResponseEntity<>(Utils.PAID_AMOUNT_REQUIRED, HttpStatus.BAD_REQUEST);
            }

            if (paymentProof == null) {
                return new ResponseEntity<>(Utils.PAYMENT_ATTACHMENT_REQUIRES, HttpStatus.BAD_REQUEST);
            }

            if (payload.paidBy() == null || payload.paidBy().isBlank()){
                return new ResponseEntity<>(Utils.PAID_BY_REQUIRED, HttpStatus.BAD_REQUEST);
            }

            if (payload.discountAmount() != null) {
                try {
                    discountAmount = Double.parseDouble(payload.discountAmount().toString());
                }
                catch (Exception e) {
                    discountAmount = 0.0;
                }
            }

            if (discountAmount < 0 || discountAmount > plans.getPrice()) {
                return new ResponseEntity<>(Utils.INVALID_DISCOUNT, HttpStatus.BAD_REQUEST);
            }

            if (plans.getPrice() > 0) {
                discountPercentage = (discountAmount / plans.getPrice()) * 100;
            }

            try {
                paidAmount = Double.parseDouble(payload.paidAmount().toString());
            } catch (Exception e) {
                paidAmount = 0.0;
            }

            double expectedAmount = plans.getPrice() - discountAmount;

            if (paidAmount < 0 || paidAmount != expectedAmount) {
                return new ResponseEntity<>(Utils.INVALID_PAID_AMOUNT, HttpStatus.BAD_REQUEST);
            }

            try {
                paymentProofUrl = uploadFileToS3.uploadFileToS3(
                        FilesConfig.convertMultipartToFileNew(paymentProof), "subscription/payment-proof");
            } catch (Exception e) {
                return new ResponseEntity<>(Utils.FILE_UPLOAD_FAILED, HttpStatus.BAD_REQUEST);
            }

            duration = plans.getDuration().intValue();
        }

        Date startsAt = today;
        if (latestSubscription.getPlanEndsAt() != null) {
            if (Utils.compareWithTwoDates(latestSubscription.getPlanEndsAt(), today) >= 0) {
                startsAt = Utils.addDaysToDate(latestSubscription.getPlanEndsAt(), 1);
            }
        }

        newSubscription.setPlanStartsAt(startsAt);
        Date endDate = Utils.addDaysToDate(startsAt, duration);
        newSubscription.setPlanEndsAt(endDate);
        newSubscription.setNextBillingAt(endDate);

        newSubscription.setPaidAmount(paidAmount);
        newSubscription.setDiscountAmount(discountAmount);
        newSubscription.setDiscount(discountPercentage);
        newSubscription.setPaymentProof(paymentProofUrl);

        newSubscription.setSubscriptionNumber(latestSubscription.getSubscriptionNumber());
        newSubscription.setHostelId(hostelId);
        newSubscription.setPlanCode(plans.getPlanCode());
        newSubscription.setPlanName(plans.getPlanName());
        newSubscription.setPlanAmount(plans.getPrice());
        newSubscription.setCreatedAt(today);
        newSubscription.setIsActive(true);
        newSubscription.setCreatedBy(agent.getAgentId());
        newSubscription.setCreatedByUserType(UserType.AGENT.name());
        newSubscription.setActivatedAt(today);

        newSubscription = subscriptionRepository.save(newSubscription);

        if (!plans.getPlanType().equalsIgnoreCase(PlanType.TRIAL.name()) &&
                !plans.getPlanType().equalsIgnoreCase(PlanType.EXPANDABLE_TRIAL.name())){

            OrderHistory newOrder = new OrderHistory();
            newOrder.setHostelId(hostelId);
            newOrder.setDiscountAmount(newSubscription.getDiscountAmount());
            newOrder.setPlanAmount(plans.getPrice());
            newOrder.setPlanCode(plans.getPlanCode());
            newOrder.setPlanName(plans.getPlanName());
            newOrder.setTotalAmount(paidAmount);
            newOrder.setOrderStatus(OrderStatus.PAID.name());
            newOrder.setPaymentType(PaymentType.MANUAL.name());
            newOrder.setChannel(Channel.CONSOLE.name());
            newOrder.setUserType(UserType.AGENT.name());
            newOrder.setPaymentProof(newSubscription.getPaymentProof());
            newOrder.setPaidBy(payload.paidBy());
            newOrder.setCollectedBy(agent.getAgentId());
            newOrder.setActive(true);
            newOrder.setCreatedAt(today);
            newOrder.setCreatedBy(agent.getAgentId());

            orderHistoryService.save(newOrder);
        }

        if (latestSubscription.getPlanEndsAt() != null) {
            if (Utils.compareWithTwoDates(latestSubscription.getPlanEndsAt(), today) < 0) {
                HostelPlan hostelPlan = hostelV1.getHostelPlan();
                if (hostelPlan == null) {
                    hostelPlan = new HostelPlan();
                    hostelPlan.setHostel(hostelV1);
                }
                hostelPlan.setCurrentPlanCode(plans.getPlanCode());
                hostelPlan.setCurrentPlanName(plans.getPlanName());
                hostelPlan.setCurrentPlanStartsAt(newSubscription.getPlanStartsAt());
                hostelPlan.setCurrentPlanEndsAt(newSubscription.getPlanEndsAt());
                hostelPlan.setCurrentPlanPrice(plans.getPrice());
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
