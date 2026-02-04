package com.smartstay.console.Mapper.hostels;

import com.smartstay.console.dao.HostelPlan;
import com.smartstay.console.dao.HostelV1;
import com.smartstay.console.dao.Users;
import com.smartstay.console.responses.hostels.HostelList;
import com.smartstay.console.responses.hostels.OwnerInfo;
import com.smartstay.console.utils.Utils;

import java.util.Date;
import java.util.List;
import java.util.function.Function;

public class HostelsListMapper implements Function<HostelV1, HostelList> {

    List<OwnerInfo> owners = null;

    public HostelsListMapper(List<OwnerInfo> owners) {
        this.owners = owners;
    }

    @Override
    public HostelList apply(HostelV1 hostelV1) {
        OwnerInfo ownerInfo = null;
        com.smartstay.console.responses.hostels.HostelPlan hp = null;
        boolean isSubscriptionActive = true;
        long noOfDaysSubscriptionActive = 0;
        StringBuilder initials = new StringBuilder();

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

        HostelPlan plan = hostelV1.getHostelPlan();
        if (plan != null) {
            hp = new com.smartstay.console.responses.hostels.HostelPlan(plan.getCurrentPlanCode(),
                    plan.getPaidAmount(),
                    plan.getCurrentPlanName());
            if (Utils.compareWithTwoDates(plan.getCurrentPlanEndsAt(), new Date()) < 0) {
                isSubscriptionActive = false;
            }
            else {
                isSubscriptionActive = true;
            }

            noOfDaysSubscriptionActive = Utils.findNumberOfDays(new Date(), plan.getCurrentPlanEndsAt());


        }
        return new HostelList(hostelV1.getHostelName(),
                hostelV1.getHostelId(),
                hostelV1.getMainImage(),
                initials.toString(),
                isSubscriptionActive,
                noOfDaysSubscriptionActive,
                null,
                null,
                ownerInfo,
                hp);
    }
}
