package com.smartstay.console.Mapper.hostels;

import com.smartstay.console.dao.HostelPlan;
import com.smartstay.console.dao.HostelV1;
import com.smartstay.console.dao.UserActivities;
import com.smartstay.console.dao.Users;
import com.smartstay.console.ennum.PlanType;
import com.smartstay.console.responses.hostels.HostelList;
import com.smartstay.console.responses.hostels.OwnerInfo;
import com.smartstay.console.utils.CountryUtils;
import com.smartstay.console.utils.Utils;

import java.util.Date;
import java.util.List;
import java.util.function.Function;

public class HostelsListMapper implements Function<HostelV1, HostelList> {

    List<OwnerInfo> owners = null;
    List<UserActivities> userActivities = null;

    public HostelsListMapper(List<OwnerInfo> owners, List<UserActivities> userActivities) {
        this.owners = owners;
        this.userActivities = userActivities;
    }

    @Override
    public HostelList apply(HostelV1 hostelV1) {
        OwnerInfo ownerInfo = null;
        String lastUpdateAt = null;
        String lastUpdateTime = null;
        String expiredOn = null;
        String expiringAt = null;
        StringBuilder fullAddress = new StringBuilder();
        com.smartstay.console.responses.hostels.HostelPlan hp = null;
        boolean isSubscriptionActive = true;
        long noOfDaysSubscriptionActive = 0;
        StringBuilder initials = new StringBuilder();
        boolean isTrial = false;

        if (hostelV1.getHouseNo() != null && !hostelV1.getHouseNo().trim().equalsIgnoreCase("")) {
            fullAddress.append(hostelV1.getHouseNo());
        }
        if (hostelV1.getHouseNo() != null && !hostelV1.getHouseNo().trim().equalsIgnoreCase("") && hostelV1.getStreet() != null) {
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

        if (owners != null) {
            ownerInfo = owners
                    .stream()
                    .filter(i -> i.parentId().equalsIgnoreCase(hostelV1.getParentId()))
                    .findFirst()
                    .orElse(null);
        }

        if (userActivities != null) {
            UserActivities ua = userActivities
                    .stream()
                    .filter(i -> i.getHostelId().equalsIgnoreCase(hostelV1.getHostelId()))
                    .findFirst()
                    .orElse(null);

            if (ua != null) {
                lastUpdateAt = Utils.dateToString(ua.getCreatedAt());
                lastUpdateTime = Utils.dateToTime(ua.getCreatedAt());
            }
        }



        HostelPlan plan = hostelV1.getHostelPlan();
        if (plan != null) {
            hp = new com.smartstay.console.responses.hostels.HostelPlan(plan.getCurrentPlanCode(),
                    plan.getPaidAmount(),
                    plan.getCurrentPlanName());
            if (plan.getCurrentPlanName().equalsIgnoreCase(PlanType.TRIAL.name())) {
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
                isSubscriptionActive,
                noOfDaysSubscriptionActive,
                lastUpdateAt,
                lastUpdateTime,
                ownerInfo,
                hp);
    }
}
