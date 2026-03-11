package com.smartstay.console.Mapper.hostels;

import com.smartstay.console.dao.*;
import com.smartstay.console.responses.hostels.HostelRecurringResponse;
import com.smartstay.console.utils.Utils;

import java.util.Date;
import java.util.Map;
import java.util.function.Function;

public class HostelRecurringMapper implements Function<BillingRules, HostelRecurringResponse> {

    HotelType hotelType;
    RecurringTracker recurringTracker;
    Map<String, Agent> agentMap;

    public HostelRecurringMapper(HotelType hotelType,
                                 RecurringTracker recurringTracker,
                                 Map<String, Agent> agentMap) {
        this.hotelType = hotelType;
        this.recurringTracker = recurringTracker;
        this.agentMap = agentMap;
    }

    @Override
    public HostelRecurringResponse apply(BillingRules billingRules) {

        HostelV1 hostel = billingRules.getHostel();
        String hostelName = hostel.getHostelName();

        String fullAddress = Utils.buildFullAddress(hostel);

        HostelPlan hostelPlan = hostel.getHostelPlan();
        boolean isSubscriptionActive = false;
        if (hostelPlan != null && hostelPlan.getCurrentPlanEndsAt() != null) {
            isSubscriptionActive = Utils.compareWithTwoDates(
                    hostelPlan.getCurrentPlanEndsAt(), new Date()) >= 0;
        }

        String hostelType = null;
        if (hotelType != null){
            hostelType = hotelType.getType();
        }

        boolean recurringStatus = false;
        Integer recurringDay = billingRules.getBillingStartDate();
        String lastRecurringDate = null;
        String recurringMode = null;
        String recurringCreatedAtDate = null;
        String recurringCreatedAtTime = null;
        String createdBy = null;
        if (recurringTracker != null){
            Date createdAt = recurringTracker.getCreatedAt();

            recurringStatus = Utils.isCurrentMonth(createdAt);

            recurringDay = recurringTracker.getCreationDay();
            recurringMode = recurringTracker.getMode();

            recurringCreatedAtDate = Utils.dateToString(createdAt);
            recurringCreatedAtTime = Utils.dateToTime(createdAt);

            if (agentMap != null){
                Agent agent = agentMap.get(recurringTracker.getCreatedBy());
                if (agent != null){
                    createdBy = Utils.getFullName(agent.getFirstName(), agent.getLastName());
                }
            }

            lastRecurringDate = Utils.dateToString(createdAt);
        }

        return new HostelRecurringResponse(hostel.getHostelId(), hostelType, hostelName, Utils.getInitials(hostelName),
                hostel.getMobile(), hostel.getEmailId(), hostel.getHouseNo(), hostel.getStreet(), hostel.getLandmark(),
                hostel.getCity(), hostel.getState(), hostel.getCountry(), hostel.getPincode(), fullAddress, hostel.getMainImage(),
                isSubscriptionActive, recurringStatus, recurringDay, lastRecurringDate, recurringMode, recurringCreatedAtDate,
                recurringCreatedAtTime, createdBy
        );
    }
}
