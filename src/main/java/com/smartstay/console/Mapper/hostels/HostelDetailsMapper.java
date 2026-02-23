package com.smartstay.console.Mapper.hostels;

import com.smartstay.console.dao.*;
import com.smartstay.console.dao.HostelPlan;
import com.smartstay.console.responses.bills.BillingRulesResponse;
import com.smartstay.console.responses.customers.CustomerResponse;
import com.smartstay.console.responses.hostels.*;
import com.smartstay.console.responses.users.UsersResponse;
import com.smartstay.console.utils.Utils;

import java.util.List;
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
    List<CustomerResponse> tenantList;
    List<Subscription> subscriptions;
    List<UsersResponse> masters;
    List<UsersResponse> staffs;

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
                               List<CustomerResponse> tenantList,
                               List<Subscription> subscriptions,
                               List<UsersResponse> masters,
                               List<UsersResponse> staffs) {
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
        this.tenantList = tenantList;
        this.subscriptions = subscriptions;
        this.masters = masters;
        this.staffs = staffs;
    }

    @Override
    public HostelResponse apply(HostelV1 hostelV1) {

        String fullAddress = buildFullAddress(hostelV1);

        HostelPlan hostelPlan = hostelV1.getHostelPlan();

        com.smartstay.console.responses.hostels.HostelPlan hostelPlanResponse = null;

        if (hostelPlan != null){
            hostelPlanResponse = new com.smartstay.console.responses.hostels.HostelPlan(
                    hostelPlan.getCurrentPlanCode(),
                    hostelPlan.getPaidAmount(),
                    hostelPlan.getCurrentPlanName()
            );
        }

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
                electricityConfig.isProRate(), electricityConfig.getCharge(), electricityConfig.getBillDate());

        List<SubscriptionResponse> subscriptionRes = subscriptions.stream()
                .map(subscription -> new SubscriptionResponse(subscription.getSubscriptionId(),
                        subscription.getSubscriptionNumber(), subscription.getPlanCode(), subscription.getPlanName(),
                        Utils.dateToString(subscription.getPlanStartsAt()), Utils.dateToString(subscription.getPlanEndsAt()),
                        subscription.getPlanAmount(), subscription.getPaidAmount()
                )).toList();

        return new HostelResponse(hostelV1.getHostelId(), hostelV1.getHostelName(), Utils.getInitials(hostelV1.getHostelName()),
                hostelV1.getMobile(), hostelV1.getHouseNo(), hostelV1.getStreet(), hostelV1.getLandmark(), hostelV1.getCity(),
                hostelV1.getState(), hostelV1.getCountry(), hostelV1.getPincode(), fullAddress, hostelV1.getMainImage(),
                addImages, noOfFloors, noOfRooms, noOfBeds, noOfActiveTenants, noOfBookedTenants, noOfCheckedInTenants,
                noOfNoticeTenants, noOfVacatedTenants, noOfTerminatedTenants, tenantList, Utils.dateToString(hostelV1.getCreatedAt()),
                Utils.dateToTime(hostelV1.getCreatedAt()), ownerInfo, masters, staffs, hostelPlanResponse, billingRules, ebConfig,
                subscriptionRes);
    }

    private static String buildFullAddress(HostelV1 hostelV1) {
        StringBuilder fullAddress = new StringBuilder();

        if (hostelV1.getHouseNo() != null && !hostelV1.getHouseNo().trim().equalsIgnoreCase("")) {
            fullAddress.append(hostelV1.getHouseNo());
        }
        if (hostelV1.getHouseNo() != null && !hostelV1.getHouseNo().trim().equalsIgnoreCase("")
                && hostelV1.getStreet() != null) {
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
        return fullAddress.toString();
    }
}
