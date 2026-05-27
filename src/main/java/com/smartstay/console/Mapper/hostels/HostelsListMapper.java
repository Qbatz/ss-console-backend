package com.smartstay.console.Mapper.hostels;

import com.smartstay.console.dao.*;
import com.smartstay.console.responses.hostelRelationalAgent.HostelRelationalAgentResponse;
import com.smartstay.console.responses.hostels.HostelList;
import com.smartstay.console.responses.hostels.OwnerInfo;
import com.smartstay.console.utils.CountryUtils;
import com.smartstay.console.utils.Utils;

import java.util.*;
import java.util.function.Function;

public class HostelsListMapper implements Function<HostelV1, HostelList> {

    OwnerInfo owner;
    UserActivities latestActivity;
    LoginHistory lastLogin;
    List<Plans> trialPlans;
    List<Plans> expandableTrialPlans;
    List<Subscription> subscriptions;
    List<HostelRelationalAgent> relationalAgents;
    Map<String, Agent> agentMap;

    public HostelsListMapper(OwnerInfo owner,
                             UserActivities latestActivity,
                             LoginHistory lastLogin,
                             List<Plans> trialPlans,
                             List<Plans> expandableTrialPlans,
                             List<Subscription> subscriptions,
                             List<HostelRelationalAgent> relationalAgents,
                             Map<String, Agent> agentMap) {
        this.owner = owner;
        this.latestActivity = latestActivity;
        this.lastLogin = lastLogin;
        this.trialPlans = trialPlans;
        this.expandableTrialPlans = expandableTrialPlans;
        this.subscriptions = subscriptions;
        this.relationalAgents = relationalAgents;
        this.agentMap = agentMap;
    }

    @Override
    public HostelList apply(HostelV1 hostelV1) {

        OwnerInfo ownerInfo = null;
        String lastUpdateAt = null;
        String lastUpdateTime = null;
        String lastUpdateDateDisplay = null;
        String expiredOn = null;
        String expiringAt = null;
        String fullAddress = null;
        com.smartstay.console.responses.hostels.HostelPlan hp = null;
        boolean isSubscriptionActive = true;
        long noOfDaysSubscriptionActive = 0;
        String initials = null;
        boolean isTrial = false;
        boolean canAddTrial = false;
        boolean canAddExpandableTrial = false;
        Date lastActivity = null;
        String platform = null;
        Set<String> trialPlanCodes = new HashSet<>();
        Set<String> expandableTrialPlanCodes = new HashSet<>();
        Date today = new Date();

        if (trialPlans != null && !trialPlans.isEmpty()) {
            trialPlans.forEach(trialPlan ->
                    trialPlanCodes.add(trialPlan.getPlanCode().toLowerCase()));

        }
        if (expandableTrialPlans != null && !expandableTrialPlans.isEmpty()) {
            expandableTrialPlans.forEach(expandableTrialPlan ->
                    expandableTrialPlanCodes.add(expandableTrialPlan.getPlanCode().toLowerCase()));

        }

        fullAddress = Utils.buildFullAddress(hostelV1);

        if (hostelV1.getHostelName() != null) {
            initials = Utils.getInitials(hostelV1.getHostelName());
        }

        if (owner != null) {
            ownerInfo = owner;
        }

        if (latestActivity != null) {
            lastUpdateAt = Utils.dateToString(latestActivity.getCreatedAt());
            lastUpdateTime = Utils.dateToTime(latestActivity.getCreatedAt());
            lastUpdateDateDisplay = Utils.formatDateDisplay(latestActivity.getCreatedAt());
            lastActivity = latestActivity.getCreatedAt();
            platform = latestActivity.getPlatform();
        }

        if (lastLogin != null) {
            if (lastActivity != null) {
                if (Utils.compareWithTwoDates(lastActivity, lastLogin.getLoginAt()) > 0) {
                    lastUpdateAt = Utils.dateToString(lastLogin.getLoginAt());
                    lastUpdateTime = Utils.dateToTime(lastLogin.getLoginAt());
                    platform = lastLogin.getPlatform();
                }
            }
            else {
                lastUpdateAt = Utils.dateToString(lastLogin.getLoginAt());
                lastUpdateTime = Utils.dateToTime(lastLogin.getLoginAt());
            }
            if (platform == null || !platform.isBlank()) {
                if (lastLogin.getSource().equalsIgnoreCase("WEB")) {
                    platform = "Web";
                }
                else {
                    platform = lastLogin.getPlatform();
                }
            }
        }

        if (platform == null) {
            platform = "NA";
        }

        HostelPlan plan = hostelV1.getHostelPlan();
        if (plan != null) {
            hp = new com.smartstay.console.responses.hostels.HostelPlan(plan.getCurrentPlanCode(),
                    plan.getPaidAmount(),
                    plan.getCurrentPlanName());

            if (trialPlanCodes.contains(plan.getCurrentPlanCode().toLowerCase())) {
                isTrial = true;
            }

            if (expandableTrialPlanCodes.contains(plan.getCurrentPlanCode().toLowerCase())) {
                isTrial = true;
            }

            if (Utils.compareWithTwoDates(plan.getCurrentPlanEndsAt(), new Date()) < 0) {
                isSubscriptionActive = false;
                expiredOn = Utils.dateToString(plan.getCurrentPlanEndsAt());
                noOfDaysSubscriptionActive = 0;
            }
            else {
                isSubscriptionActive = true;
                expiringAt = Utils.dateToString(plan.getCurrentPlanEndsAt());
                noOfDaysSubscriptionActive = Utils.findNumberOfDays(new Date(), plan.getCurrentPlanEndsAt());
            }
        }

        Date todayStart = Utils.getStartOfDay(today);

        if (subscriptions != null) {
            long trialCount = 0;
            long subscriptionCount = 0;
            long subPendingCount = 0;

            Set<String> allTrialPlanCodes = new HashSet<>();
            allTrialPlanCodes.addAll(trialPlanCodes);
            allTrialPlanCodes.addAll(expandableTrialPlanCodes);

            for (Subscription subscription : subscriptions) {
                if (trialPlanCodes.contains(subscription.getPlanCode().toLowerCase())) {
                    trialCount++;
                }
                if (!allTrialPlanCodes.contains(subscription.getPlanCode().toLowerCase())) {
                    subscriptionCount++;
                }
                Date planStart = subscription.getPlanStartsAt();
                if (planStart != null && !planStart.before(todayStart)) {
                    subPendingCount++;
                }
            }

            canAddTrial = (trialCount < 2) && (subPendingCount == 0);
            canAddExpandableTrial = (subscriptionCount == 0) && (subPendingCount == 0);
        }

        List<HostelRelationalAgentResponse> relationalAgentResponses = new ArrayList<>();
        if (relationalAgents != null) {
            relationalAgentResponses = relationalAgents.stream()
                    .sorted(Comparator.comparing(HostelRelationalAgent::getId).reversed())
                    .map(hostelRelationalAgent -> {
                        Agent relationalAgent = agentMap.getOrDefault(hostelRelationalAgent.getAgentId(), null);
                        Agent createdByAgent = agentMap.getOrDefault(hostelRelationalAgent.getCreatedBy(), null);

                        String relationalAgentName = null;
                        if (relationalAgent != null){
                            relationalAgentName = Utils.getFullName(relationalAgent.getFirstName(), relationalAgent.getLastName());
                        }

                        String createdByAgentName = null;
                        if (createdByAgent != null){
                            createdByAgentName = Utils.getFullName(createdByAgent.getFirstName(), createdByAgent.getLastName());
                        }

                        return new HostelRelationalAgentResponse(hostelRelationalAgent.getId(), hostelRelationalAgent.getParentId(),
                                hostelRelationalAgent.getAgentId(), relationalAgentName, hostelRelationalAgent.getReason().name(),
                                hostelRelationalAgent.getComments(), createdByAgentName, Utils.dateToString(hostelRelationalAgent.getCreatedAt()),
                                Utils.dateToTime(hostelRelationalAgent.getCreatedAt()));
                    }).toList();
        }

        return new HostelList(hostelV1.getHostelName(),
                hostelV1.getHostelId(),
                hostelV1.getMainImage(),
                initials.toString(),
                CountryUtils.COUNTRY_CODE_IN,
                hostelV1.getMobile(),
                fullAddress.toString(),
                hostelV1.getCity(),
                hostelV1.getState(),
                Utils.dateToString(hostelV1.getCreatedAt()),
                expiredOn,
                expiringAt,
                isTrial,
                canAddTrial,
                canAddExpandableTrial,
                isSubscriptionActive,
                noOfDaysSubscriptionActive,
                lastUpdateAt,
                lastUpdateTime,
                lastUpdateDateDisplay,
                platform,
                ownerInfo,
                hp,
                relationalAgentResponses);
    }
}
