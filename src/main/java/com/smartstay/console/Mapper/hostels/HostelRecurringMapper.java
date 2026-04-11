package com.smartstay.console.Mapper.hostels;

import com.smartstay.console.Mapper.users.UserOnerInfoMapper;
import com.smartstay.console.dao.*;
import com.smartstay.console.dto.hostel.BillingDates;
import com.smartstay.console.ennum.BillingModel;
import com.smartstay.console.responses.hostels.HostelRecurringResponse;
import com.smartstay.console.responses.hostels.OwnerInfo;
import com.smartstay.console.utils.Utils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class HostelRecurringMapper implements Function<BillingRules, HostelRecurringResponse> {

    Users owner;
    HotelType hotelType;
    RecurringTracker recurringTracker;
    Map<String, Agent> agentMap;
    List<BookingsV1> bookings;
    BillingDates billingDates;

    public HostelRecurringMapper(Users owner,
                                 HotelType hotelType,
                                 RecurringTracker recurringTracker,
                                 Map<String, Agent> agentMap,
                                 List<BookingsV1> bookings,
                                 BillingDates billingDates) {
        this.owner = owner;
        this.hotelType = hotelType;
        this.recurringTracker = recurringTracker;
        this.agentMap = agentMap;
        this.bookings = bookings;
        this.billingDates = billingDates;
    }

    @Override
    public HostelRecurringResponse apply(BillingRules billingRules) {

        HostelV1 hostel = billingRules.getHostel();
        String hostelName = hostel.getHostelName();

        String fullAddress = Utils.buildFullAddress(hostel);

        OwnerInfo ownerInfo = null;
        if (owner != null){
            ownerInfo = new UserOnerInfoMapper().apply(owner);
        }

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

        boolean isPostpaid = BillingModel.POSTPAID.name().equals(billingRules.getBillingModel());

        Date today = new Date();

        int startDay = billingRules.getBillingStartDate();
        Date cycleStartDate = today;
        int endDay;
        if (billingDates != null){
            cycleStartDate = billingDates.currentBillStartDate();
        } else {
            if (isPostpaid) {
                cycleStartDate = Utils.getPreviousMonthDate(today);
            }
        }
        endDay = Utils.calculateEndDay(startDay, cycleStartDate);

        boolean recurringStatus = false;
        Integer recurringDay = billingRules.getBillingStartDate();

        String lastRecurringDate = null;
        String recurringMode = null;
        String recurringCreatedAtDate = null;
        String recurringCreatedAtTime = null;
        String createdBy = null;
        if (recurringTracker != null){
            Date createdAt = recurringTracker.getCreatedAt();

            recurringStatus = Utils.isSameBillingCycle(billingRules.getBillingStartDate(),
                    recurringTracker, cycleStartDate);

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

        int noOfActiveTenants = bookings != null ? bookings.size() : 0;

        int invoiceAboutToBeGenerated = 0;
        if (bookings != null){
            int billingDay = billingRules.getBillingStartDate();

            if (!isPostpaid){
                invoiceAboutToBeGenerated = (int) bookings.stream()
                        .filter(customer -> Utils.isEligibleForInvoice(customer, billingDay))
                        .count();
            } else {
                invoiceAboutToBeGenerated = bookings.size();
            }
        }

        return new HostelRecurringResponse(hostel.getHostelId(), hostelType, hostelName, Utils.getInitials(hostelName),
                hostel.getMobile(), hostel.getEmailId(), hostel.getHouseNo(), hostel.getStreet(), hostel.getLandmark(),
                hostel.getCity(), hostel.getState(), hostel.getCountry(), hostel.getPincode(), fullAddress, hostel.getMainImage(),
                ownerInfo, noOfActiveTenants, invoiceAboutToBeGenerated, startDay, endDay, billingRules.getTypeOfBilling(),
                billingRules.getBillingModel(), isSubscriptionActive, recurringStatus, recurringDay, lastRecurringDate,
                recurringMode, recurringCreatedAtDate, recurringCreatedAtTime, createdBy
        );
    }
}
