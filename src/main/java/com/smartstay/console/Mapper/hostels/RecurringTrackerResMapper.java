package com.smartstay.console.Mapper.hostels;

import com.smartstay.console.Mapper.users.UserOnerInfoMapper;
import com.smartstay.console.dao.*;
import com.smartstay.console.ennum.BookingsStatus;
import com.smartstay.console.responses.hostels.OwnerInfo;
import com.smartstay.console.responses.hostels.RecurringHistoryRes;
import com.smartstay.console.responses.hostels.RecurringTrackerRes;
import com.smartstay.console.utils.Utils;
import org.springframework.data.domain.Page;

import java.util.Date;
import java.util.List;
import java.util.function.Function;

public class RecurringTrackerResMapper implements Function<HostelV1, RecurringTrackerRes> {

    HotelType hotelType;
    Users owner;
    List<BookingsV1> bookings;
    List<BookingsV1> customers;
    BillingRules billingRules;
    RecurringTracker latestRecurringTracker;
    int page;
    int size;
    Page<RecurringTracker> paginatedRecurringTrackers;
    List<RecurringHistoryRes> recurringHistory;

    public RecurringTrackerResMapper(HotelType hotelType,
                                     Users owner,
                                     List<BookingsV1> bookings,
                                     List<BookingsV1> customers,
                                     BillingRules billingRules,
                                     RecurringTracker latestRecurringTracker,
                                     int page,
                                     int size,
                                     Page<RecurringTracker> paginatedRecurringTrackers,
                                     List<RecurringHistoryRes> recurringHistory) {
        this.hotelType = hotelType;
        this.owner = owner;
        this.bookings = bookings;
        this.customers = customers;
        this.billingRules = billingRules;
        this.latestRecurringTracker = latestRecurringTracker;
        this.page = page;
        this.size = size;
        this.paginatedRecurringTrackers = paginatedRecurringTrackers;
        this.recurringHistory = recurringHistory;
    }

    @Override
    public RecurringTrackerRes apply(HostelV1 hostel) {

        String hostelType = null;
        if (hotelType != null){
            hostelType = hotelType.getType();
        }

        String hostelName = hostel.getHostelName();

        String fullAddress = Utils.buildFullAddress(hostel);

        OwnerInfo ownerInfo = null;
        if (owner != null){
            ownerInfo = new UserOnerInfoMapper().apply(owner);
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

        int invoiceAboutToBeGenerated = 0;
        if (customers != null){
            int billingDay = billingRules.getBillingStartDate();

            invoiceAboutToBeGenerated = (int) customers.stream()
                    .filter(customer -> Utils.isEligibleForInvoice(customer, billingDay))
                    .count();
        }

        Date today = new Date();
        int startDay = billingRules.getBillingStartDate();
        int endDay = Utils.calculateEndDay(startDay, today);

        int month = Utils.getCurrentMonth(today);
        int year = Utils.getCurrentYear(today);

        Date startDate = Utils.getDateFromDay(startDay, month, year);
        Date endDate = Utils.getEndDate(startDay, month, year);

        HostelPlan hostelPlan = hostel.getHostelPlan();
        boolean isSubscriptionActive = false;
        if (hostelPlan != null && hostelPlan.getCurrentPlanEndsAt() != null) {
            isSubscriptionActive = Utils.compareWithTwoDates(
                    hostelPlan.getCurrentPlanEndsAt(), new Date()) >= 0;
        }

        Date lastRecurringDate = null;
        Date nextRecurringDate = null;
        boolean recurringStatus = false;
        if (latestRecurringTracker != null){
            lastRecurringDate = Utils.getDateFromDay(latestRecurringTracker.getCreationDay(),
                    latestRecurringTracker.getCreationMonth(), latestRecurringTracker.getCreationYear());
            nextRecurringDate = Utils.getNextMonthDate(latestRecurringTracker.getCreationDay(),
                    latestRecurringTracker.getCreationMonth(), latestRecurringTracker.getCreationYear());
            recurringStatus = Utils.isSameBillingCycle(
                    billingRules.getBillingStartDate(), latestRecurringTracker);
        }

        return new RecurringTrackerRes(hostel.getHostelId(), hostelType, hostelName, Utils.getInitials(hostelName),
                hostel.getMobile(), hostel.getEmailId(), hostel.getHouseNo(), hostel.getStreet(), hostel.getLandmark(),
                hostel.getCity(), hostel.getState(), hostel.getCountry(), hostel.getPincode(), fullAddress, hostel.getMainImage(),
                ownerInfo, noOfActiveTenants, invoiceAboutToBeGenerated, billingRules.getTypeOfBilling(), startDay, endDay,
                Utils.dateToString(startDate), Utils.dateToString(endDate), Utils.dateToString(lastRecurringDate),
                Utils.dateToString(nextRecurringDate), isSubscriptionActive, recurringStatus, page, size,
                paginatedRecurringTrackers.getTotalElements(), paginatedRecurringTrackers.getTotalPages(), recurringHistory);
    }
}
