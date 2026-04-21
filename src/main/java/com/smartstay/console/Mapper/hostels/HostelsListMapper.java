package com.smartstay.console.Mapper.hostels;

import com.smartstay.console.dao.*;
import com.smartstay.console.responses.hostels.HostelList;
import com.smartstay.console.responses.hostels.OwnerInfo;
import com.smartstay.console.utils.CountryUtils;
import com.smartstay.console.utils.Utils;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class HostelsListMapper implements Function<HostelV1, HostelList> {

    OwnerInfo owner;
    UserActivities latestActivity;
    LoginHistory lastLogin;
    List<Plans> trialPlans;
    List<Plans> expandableTrialPlans;
    List<Subscription> subscriptions;

    public HostelsListMapper(OwnerInfo owner,
                             UserActivities latestActivity,
                             LoginHistory lastLogin,
                             List<Plans> trialPlans,
                             List<Plans> expandableTrialPlans,
                             List<Subscription> subscriptions) {
        this.owner = owner;
        this.latestActivity = latestActivity;
        this.lastLogin = lastLogin;
        this.trialPlans = trialPlans;
        this.expandableTrialPlans = expandableTrialPlans;
        this.subscriptions = subscriptions;
    }

    @Override
    public HostelList apply(HostelV1 hostelV1) {

        OwnerInfo ownerInfo = null;
        String lastUpdateAt = null;
        String lastUpdateTime = null;
        String lastUpdateDateDisplay = null;
        String expiredOn = null;
        String expiringAt = null;
        StringBuilder fullAddress = new StringBuilder();
        com.smartstay.console.responses.hostels.HostelPlan hp = null;
        boolean isSubscriptionActive = true;
        long noOfDaysSubscriptionActive = 0;
        StringBuilder initials = new StringBuilder();
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

        if (hostelV1.getHouseNo() != null && !hostelV1.getHouseNo().trim().equalsIgnoreCase("")) {
            fullAddress.append(hostelV1.getHouseNo());
        }
        if (hostelV1.getHouseNo() != null && !hostelV1.getHouseNo().trim().equalsIgnoreCase("") &&
                hostelV1.getStreet() != null) {
            fullAddress.append(", ");
            fullAddress.append(hostelV1.getStreet());
        }
        else {
            fullAddress.append(hostelV1.getStreet());
        }
        if (hostelV1.getCity() != null) {
            if (fullAddress.isEmpty()) {
                fullAddress.append(hostelV1.getCity());
            }
            else {
                fullAddress.append(", ");
                fullAddress.append(hostelV1.getCity());
            }
        }
        if (hostelV1.getState() != null) {
            if (fullAddress.isEmpty()) {
                fullAddress.append(hostelV1.getState());
            }
            else {
                fullAddress.append(", ");
                fullAddress.append(hostelV1.getState());
            }
        }

        if (hostelV1.getHostelName() != null) {
            String[] arrName = hostelV1.getHostelName().split(" ");
            if (arrName.length > 0) {
                initials.append(arrName[0].toUpperCase().charAt(0));
            }
            if (arrName.length > 1) {
                initials.append(arrName[arrName.length - 1].toUpperCase().charAt(0));
            }
            else {
                initials.append(arrName[arrName.length - 1].toUpperCase().charAt(1));
            }
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
                hp);
    }
}
