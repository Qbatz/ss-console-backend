package com.smartstay.console.Mapper.hostels;

import com.smartstay.console.dao.*;
import com.smartstay.console.dao.HostelPlan;
import com.smartstay.console.dto.hostel.BillingDates;
import com.smartstay.console.responses.bills.BillingRulesResponse;
import com.smartstay.console.responses.customers.CustomerRecHistoryRes;
import com.smartstay.console.responses.customers.CustomerResponse;
import com.smartstay.console.responses.hostels.*;
import com.smartstay.console.responses.users.UserActivitiesResponse;
import com.smartstay.console.responses.users.UsersResponse;
import com.smartstay.console.utils.Utils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class HostelDetailsMapper implements Function<HostelV1, HostelResponse> {

    OwnerInfo ownerInfo;
    int noOfFloors;
    int noOfRooms;
    int noOfBeds;
    int noOfActiveTenants;
    int noOfBookedTenants;
    int noOfCheckedInTenants;
    int noOfNoticeTenants;
    int noOfVacatedTenants;
    int noOfTerminatedTenants;
    List<SharingTypeResponse> sharingTypeList;
    List<AmenitiesV1> amenities;
    List<CustomerResponse> tenantList;
    List<Subscription> subscriptions;
    List<UsersResponse> masters;
    List<UsersResponse> staffs;
    List<UserActivities> activities;
    Map<String, Users> userLookup;
    List<Plans> trialPlans;
    List<Plans> expandableTrialPlans;
    Map<Integer, BillingDates> billingDatesMap;
    List<RecurringHistoryRes> recurringHistory;
    List<CustomerRecHistoryRes> customerRecurringHistory;
    boolean recurringStatus;
    Date currentBillLastRecDate;
    BillingRules currentBillingRules;

    public HostelDetailsMapper(OwnerInfo ownerInfo,
                               int noOfFloors,
                               int noOfRooms,
                               int noOfBeds,
                               int noOfActiveTenants,
                               int noOfBookedTenants,
                               int noOfCheckedInTenants,
                               int noOfNoticeTenants,
                               int noOfVacatedTenants,
                               int noOfTerminatedTenants,
                               List<SharingTypeResponse> sharingTypeList,
                               List<AmenitiesV1> amenities,
                               List<CustomerResponse> tenantList,
                               List<Subscription> subscriptions,
                               List<UsersResponse> masters,
                               List<UsersResponse> staffs,
                               List<UserActivities> activities,
                               Map<String, Users> userLookup,
                               List<Plans> trialPlans,
                               List<Plans> expandableTrialPlans,
                               Map<Integer, BillingDates> billingDatesMap,
                               List<RecurringHistoryRes> recurringHistory,
                               List<CustomerRecHistoryRes> customerRecurringHistory,
                               boolean recurringStatus,
                               Date currentBillLastRecDate,
                               BillingRules currentBillingRules) {
        this.ownerInfo = ownerInfo;
        this.noOfFloors = noOfFloors;
        this.noOfRooms = noOfRooms;
        this.noOfBeds = noOfBeds;
        this.noOfActiveTenants = noOfActiveTenants;
        this.noOfBookedTenants = noOfBookedTenants;
        this.noOfCheckedInTenants = noOfCheckedInTenants;
        this.noOfNoticeTenants = noOfNoticeTenants;
        this.noOfVacatedTenants = noOfVacatedTenants;
        this.noOfTerminatedTenants = noOfTerminatedTenants;
        this.sharingTypeList = sharingTypeList;
        this.amenities = amenities;
        this.tenantList = tenantList;
        this.subscriptions = subscriptions;
        this.masters = masters;
        this.staffs = staffs;
        this.activities = activities;
        this.userLookup = userLookup;
        this.trialPlans = trialPlans;
        this.expandableTrialPlans = expandableTrialPlans;
        this.billingDatesMap = billingDatesMap;
        this.recurringHistory = recurringHistory;
        this.customerRecurringHistory = customerRecurringHistory;
        this.recurringStatus = recurringStatus;
        this.currentBillLastRecDate = currentBillLastRecDate;
        this.currentBillingRules = currentBillingRules;
    }

    @Override
    public HostelResponse apply(HostelV1 hostelV1) {

        String fullAddress = Utils.buildFullAddress(hostelV1);

        List<HostelImages> additionalImages = hostelV1.getAdditionalImages();

        List<HostelImagesResponse> addImages = additionalImages.stream()
                .map(image -> new HostelImagesResponse(
                        image.getId(), image.getImageUrl()
                )).toList();

        List<BillingRules> billingRulesList = hostelV1.getBillingRulesList();

        List<BillingRulesResponse> billingRules = billingRulesList.stream()
                .sorted(Comparator.comparing(BillingRules::getId).reversed())
                .map(billRules -> {

                    boolean isCurrent = false;
                    if (currentBillingRules != null){
                        isCurrent = Objects.equals(currentBillingRules.getId(), billRules.getId());
                    }

                    Integer billingStartDay = null;
                    Integer billingEndDay = null;
                    String currentPeriodStartDate = null;
                    String currentPeriodEndDate = null;
                    String lastRecurringDate = null;
                    String nextRecurringDate = null;

                    if (billingDatesMap != null) {
                        BillingDates billingDates = billingDatesMap.getOrDefault(billRules.getId(), null);

                        if (billingDates != null) {
                            Date cycleStartDate = billingDates.currentBillStartDate();
                            Date cycleEndDate = billingDates.currentBillEndDate();

                            billingStartDay = billRules.getBillingStartDate();
                            billingEndDay = Utils.calculateEndDay(billingStartDay, cycleStartDate);
                            currentPeriodStartDate = Utils.dateToString(cycleStartDate);
                            currentPeriodEndDate = Utils.dateToString(cycleEndDate);

                            lastRecurringDate = Utils.dateToString(cycleStartDate);
                            Date nextRecurring = Utils.getNextMonthDate(cycleStartDate);
                            nextRecurringDate = Utils.dateToString(nextRecurring);
                        }
                    }

                    if (isCurrent && currentBillLastRecDate != null) {
                        lastRecurringDate = Utils.dateToString(currentBillLastRecDate);
                    }

                    String billRulesCreatedBy = null;
                    if (billRules.getCreatedBy() != null){
                        Users billRulesCreatedByUser = userLookup.getOrDefault(billRules.getCreatedBy(), null);
                        if (billRulesCreatedByUser != null){
                            billRulesCreatedBy = Utils
                                    .getFullName(billRulesCreatedByUser.getFirstName(), billRulesCreatedByUser.getLastName());
                        }
                    }

                    return new BillingRulesResponse(
                            billRules.getId(), billRules.getBillingStartDate(), billRules.getBillDueDays(),
                            billRules.getNoticePeriod(), billingStartDay, billingEndDay, currentPeriodStartDate, currentPeriodEndDate,
                            lastRecurringDate, nextRecurringDate, billRules.isInitial(), billRules.isHasGracePeriod(),
                            billRules.getGracePeriodDays(), billRules.getTypeOfBilling(), billRules.getBillingModel(),
                            billRules.getReminderDays(), billRules.isShouldNotify(), Utils.dateToString(billRules.getCreatedAt()),
                            Utils.dateToTime(billRules.getCreatedAt()), billRulesCreatedBy);
                }).toList();

        BillingRulesResponse currentBillingRulesRes;
        if (currentBillingRules != null) {
            currentBillingRulesRes = billingRules.isEmpty() ? null : billingRules.stream()
                            .filter(br -> Objects.equals(currentBillingRules.getId(), br.billingRulesId()))
                            .findFirst().orElse(null);
        } else {
            currentBillingRulesRes = billingRules.isEmpty() ? null : billingRules.getFirst();
        }

        ElectricityConfig electricityConfig = hostelV1.getElectricityConfig();

        String ebConfigUpdatedBy = null;
        if (electricityConfig.getUpdatedBy() != null){
            Users ebConfigUpdatedByUser = userLookup.getOrDefault(electricityConfig.getUpdatedBy(), null);
            if (ebConfigUpdatedByUser != null){
                ebConfigUpdatedBy = Utils
                        .getFullName(ebConfigUpdatedByUser.getFirstName(), ebConfigUpdatedByUser.getLastName());
            }
        }

        EbConfig ebConfig = new EbConfig(electricityConfig.getId(), electricityConfig.isShouldIncludeInRent(),
                electricityConfig.getTypeOfReading(), electricityConfig.getCharge(), electricityConfig.getFlatCharge(),
                electricityConfig.getBillDate(), electricityConfig.isUpdated(), Utils.dateToString(electricityConfig.getLastUpdate()),
                Utils.dateToTime(electricityConfig.getLastUpdate()), ebConfigUpdatedBy);

        String subscriptionStatus;
        int subscriptionRenewalTimeLeftDays = 0;

        Date today = new Date();

        List<Subscription> sortedSubs = subscriptions.stream()
                .sorted(Comparator.comparing(Subscription::getPlanStartsAt))
                .toList();

        boolean hasActive = false;

        Subscription currentSubscription = null;
        List<Subscription> otherSubscriptions = new ArrayList<>();

        for (Subscription sub : sortedSubs) {

            Date start = sub.getPlanStartsAt();
            Date end = sub.getPlanEndsAt();

            // CASE 1: already expired -> ignore
            if (Utils.compareWithTwoDates(end, today) < 0) {
                otherSubscriptions.add(sub);
                continue;
            }

            // CASE 2: currently active (start <= today AND end >= today)
            if (Utils.compareWithTwoDates(start, today) <= 0 &&
                    Utils.compareWithTwoDates(end, today) >= 0) {

                hasActive = true;
                subscriptionRenewalTimeLeftDays +=
                        (int) Utils.findNumberOfDays(today, end);

                currentSubscription = sub;
                otherSubscriptions.add(sub);
                continue;
            }

            // CASE 3: future subscription
            if (Utils.compareWithTwoDates(start, today) > 0) {

                subscriptionRenewalTimeLeftDays +=
                        (int) Utils.findNumberOfDays(start, end);

                otherSubscriptions.add(sub);
            }
        }

        if (hasActive) {
            subscriptionStatus = Utils.SUBSCRIPTION_ACTIVE;
        } else {
            subscriptionStatus = Utils.SUBSCRIPTION_INACTIVE;
        }

        SubscriptionResponse currentSubRes = null;

        if (currentSubscription != null) {
            currentSubRes = new SubscriptionResponse(
                    currentSubscription.getSubscriptionId(),
                    currentSubscription.getSubscriptionNumber(),
                    currentSubscription.getPlanCode(),
                    currentSubscription.getPlanName(),
                    Utils.dateToString(currentSubscription.getPlanStartsAt()),
                    Utils.dateToString(currentSubscription.getPlanEndsAt()),
                    currentSubscription.getPlanAmount(),
                    currentSubscription.getPaidAmount(),
                    currentSubscription.getDiscount(),
                    currentSubscription.getDiscountAmount(),
                    currentSubscription.getPaymentProof()
            );
        }

        List<SubscriptionResponse> otherSubsRes = otherSubscriptions.stream()
                .sorted(Comparator.comparing(Subscription::getPlanStartsAt).reversed())
                .map(subscription -> new SubscriptionResponse(
                        subscription.getSubscriptionId(),
                        subscription.getSubscriptionNumber(),
                        subscription.getPlanCode(),
                        subscription.getPlanName(),
                        Utils.dateToString(subscription.getPlanStartsAt()),
                        Utils.dateToString(subscription.getPlanEndsAt()),
                        subscription.getPlanAmount(),
                        subscription.getPaidAmount(),
                        subscription.getDiscount(),
                        subscription.getDiscountAmount(),
                        subscription.getPaymentProof()
                )).toList();

        List<UserActivitiesResponse> activitiesRes = activities.stream()
                .map(activity -> {
                    Users user = userLookup.get(activity.getUserId());
                    String userName = null;
                    if (user != null){
                        userName = Utils.getFullName(user.getFirstName(), user.getLastName());
                    }
                    return new UserActivitiesResponse(
                            activity.getActivityId(), activity.getDescription(), activity.getUserId(), userName,
                            Utils.dateToString(activity.getCreatedAt()), Utils.dateToTime(activity.getCreatedAt()),
                            activity.getSource(), activity.getActivityType()
                    );
                }).toList();

        List<AmenitiesResponse> amenitiesRes = amenities.stream()
                .map(amenity -> new AmenitiesResponse(amenity.getAmenityId(), amenity.getAmenityName(),
                        amenity.getAmenityAmount(), amenity.getDescription(), amenity.getTermsAndCondition(),
                        amenity.getIsProRate()))
                .toList();

        boolean isTrial = false;
        boolean canAddTrial = false;
        boolean canAddExpandableTrial = false;
        Set<String> trialPlanCodes = new HashSet<>();
        Set<String> expandableTrialPlanCodes = new HashSet<>();

        if (trialPlans != null && !trialPlans.isEmpty()) {
            trialPlans.forEach(trialPlan ->
                    trialPlanCodes.add(trialPlan.getPlanCode().toLowerCase()));

        }
        if (expandableTrialPlans != null && !expandableTrialPlans.isEmpty()) {
            expandableTrialPlans.forEach(expandableTrialPlan ->
                    expandableTrialPlanCodes.add(expandableTrialPlan.getPlanCode().toLowerCase()));

        }

        boolean isSubscriptionActive = false;

        HostelPlan currentPlan = hostelV1.getHostelPlan();

        if (currentPlan != null) {
            if (trialPlanCodes.contains(currentPlan.getCurrentPlanCode().toLowerCase())) {
                isTrial = true;
            }

            if (expandableTrialPlanCodes.contains(currentPlan.getCurrentPlanCode().toLowerCase())) {
                isTrial = true;
            }

            if (currentPlan.getCurrentPlanEndsAt() != null) {
                isSubscriptionActive = Utils.compareWithTwoDates(
                        currentPlan.getCurrentPlanEndsAt(), new Date()) >= 0;
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

        return new HostelResponse(hostelV1.getHostelId(), hostelV1.getHostelName(), Utils.getInitials(hostelV1.getHostelName()),
                hostelV1.getMobile(), hostelV1.getHouseNo(), hostelV1.getStreet(), hostelV1.getLandmark(), hostelV1.getCity(),
                hostelV1.getState(), hostelV1.getCountry(), hostelV1.getPincode(), fullAddress, hostelV1.getMainImage(), isTrial,
                canAddTrial, canAddExpandableTrial, addImages, amenitiesRes, sharingTypeList, noOfFloors, noOfRooms, noOfBeds,
                noOfActiveTenants, noOfBookedTenants, noOfCheckedInTenants, noOfNoticeTenants, noOfVacatedTenants, noOfTerminatedTenants,
                tenantList, Utils.dateToString(hostelV1.getCreatedAt()), Utils.dateToTime(hostelV1.getCreatedAt()), ownerInfo, masters,
                staffs, currentBillingRulesRes, billingRules, ebConfig, currentSubRes, otherSubsRes, subscriptionStatus,
                subscriptionRenewalTimeLeftDays, isSubscriptionActive, recurringStatus, recurringHistory, customerRecurringHistory,
                activitiesRes);
    }
}
