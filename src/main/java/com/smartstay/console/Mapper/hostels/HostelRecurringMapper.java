package com.smartstay.console.Mapper.hostels;

import com.smartstay.console.Mapper.users.UserOnerInfoMapper;
import com.smartstay.console.dao.*;
import com.smartstay.console.ennum.BookingsStatus;
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
    List<BookingsV1> customers;

    public HostelRecurringMapper(Users owner,
                                 HotelType hotelType,
                                 RecurringTracker recurringTracker,
                                 Map<String, Agent> agentMap,
                                 List<BookingsV1> bookings,
                                 List<BookingsV1> customers) {
        this.owner = owner;
        this.hotelType = hotelType;
        this.recurringTracker = recurringTracker;
        this.agentMap = agentMap;
        this.bookings = bookings;
        this.customers = customers;
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

        boolean recurringStatus = false;
        Integer recurringDay = billingRules.getBillingStartDate();
        String lastRecurringDate = null;
        String recurringMode = null;
        String recurringCreatedAtDate = null;
        String recurringCreatedAtTime = null;
        String createdBy = null;
        if (recurringTracker != null){
            Date createdAt = recurringTracker.getCreatedAt();

            recurringStatus = Utils.isSameBillingCycle(
                    billingRules.getBillingStartDate(), recurringTracker);

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

        int noOfBookedTenants = 0;
        int noOfCheckedInTenants = 0;
        if (bookings != null && !bookings.isEmpty()){
            for (BookingsV1 booking : bookings){
                if (booking.getCurrentStatus().equalsIgnoreCase(BookingsStatus.BOOKED.name())){
                    noOfBookedTenants++;
                } else if (booking.getCurrentStatus().equalsIgnoreCase(BookingsStatus.CHECKIN.name())) {
                    noOfCheckedInTenants++;
                }
            }
        }

        int noOfActiveTenants = noOfBookedTenants + noOfCheckedInTenants;

        Date today = new Date();
        int startDay = billingRules.getBillingStartDate();
        int endDay = Utils.calculateEndDay(startDay, today);

        int invoiceAboutToBeGenerated = 0;
        if (customers != null){
            int billingDay = billingRules.getBillingStartDate();

            invoiceAboutToBeGenerated = (int) customers.stream()
                    .filter(customer -> Utils.isEligibleForInvoice(customer, billingDay))
                    .count();
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
