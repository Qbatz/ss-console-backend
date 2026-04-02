package com.smartstay.console.Mapper.hostels;

import com.smartstay.console.dao.*;
import com.smartstay.console.dao.HostelPlan;
import com.smartstay.console.responses.bills.BillingRulesResponse;
import com.smartstay.console.responses.customers.CustomerResponse;
import com.smartstay.console.responses.hostels.*;
import com.smartstay.console.responses.users.UserActivitiesResponse;
import com.smartstay.console.responses.users.UsersResponse;
import com.smartstay.console.utils.Utils;

import java.util.*;
import java.util.function.Function;

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
    Plans trialPlan;
    Plans trialDaysPlan;

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
                               Plans trialPlan,
                               Plans trialDaysPlan) {
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
        this.trialPlan = trialPlan;
        this.trialDaysPlan = trialDaysPlan;
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
                .map(billRules -> new BillingRulesResponse(
                        billRules.getId(), billRules.getBillingStartDate(),
                        billRules.getBillDueDays(), billRules.getNoticePeriod()
                )).toList();

        ElectricityConfig electricityConfig = hostelV1.getElectricityConfig();

        EbConfig ebConfig = new EbConfig(electricityConfig.getId(),
                electricityConfig.isShouldIncludeInRent(), electricityConfig.getTypeOfReading(),
                electricityConfig.getCharge(), electricityConfig.getBillDate());

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

        boolean trialExtendable = false;
        Set<String> trialPlanCodes = new HashSet<>();

        if (trialPlan != null) {
            trialPlanCodes.add(trialPlan.getPlanCode().toLowerCase());
        }
        if (trialDaysPlan != null) {
            trialPlanCodes.add(trialDaysPlan.getPlanCode().toLowerCase());
        }

        HostelPlan currentPlan = hostelV1.getHostelPlan();
        if (currentPlan != null) {
            if (trialPlanCodes.contains(currentPlan.getCurrentPlanCode().toLowerCase())) {
                if (subscriptions != null) {
                    long trialCount = 0;
                    long subscriptionCount = 0;

                    for (Subscription subscription : subscriptions) {
                        if (trialPlanCodes.contains(subscription.getPlanCode().toLowerCase())) {
                            trialCount++;
                        } else {
                            subscriptionCount++;
                        }
                    }

                    if (trialCount < 2) {
                        trialExtendable = true;
                    }
                    if (subscriptionCount > 0) {
                        trialExtendable = false;
                    }
                }
            }
        }

        return new HostelResponse(hostelV1.getHostelId(), hostelV1.getHostelName(), Utils.getInitials(hostelV1.getHostelName()),
                hostelV1.getMobile(), hostelV1.getHouseNo(), hostelV1.getStreet(), hostelV1.getLandmark(), hostelV1.getCity(),
                hostelV1.getState(), hostelV1.getCountry(), hostelV1.getPincode(), fullAddress, hostelV1.getMainImage(),
                trialExtendable, addImages, amenitiesRes, sharingTypeList, noOfFloors, noOfRooms, noOfBeds, noOfActiveTenants,
                noOfBookedTenants, noOfCheckedInTenants, noOfNoticeTenants, noOfVacatedTenants, noOfTerminatedTenants, tenantList,
                Utils.dateToString(hostelV1.getCreatedAt()), Utils.dateToTime(hostelV1.getCreatedAt()), ownerInfo, masters, staffs,
                billingRules, ebConfig, currentSubRes, otherSubsRes, subscriptionStatus, subscriptionRenewalTimeLeftDays,
                activitiesRes);
    }
}
