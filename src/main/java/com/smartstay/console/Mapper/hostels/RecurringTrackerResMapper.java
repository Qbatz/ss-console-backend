package com.smartstay.console.Mapper.hostels;

import com.smartstay.console.Mapper.users.UserOnerInfoMapper;
import com.smartstay.console.dao.*;
import com.smartstay.console.dto.hostel.BillingDates;
import com.smartstay.console.ennum.BillingModel;
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
    BillingRules billingRules;
    BillingDates billingDates;
    RecurringTracker latestRecurringTracker;
    int page;
    int size;
    Page<RecurringTracker> paginatedRecurringTrackers;
    List<RecurringHistoryRes> recurringHistory;

    public RecurringTrackerResMapper(HotelType hotelType,
                                     Users owner,
                                     List<BookingsV1> bookings,
                                     BillingRules billingRules,
                                     BillingDates billingDates,
                                     RecurringTracker latestRecurringTracker,
                                     int page,
                                     int size,
                                     Page<RecurringTracker> paginatedRecurringTrackers,
                                     List<RecurringHistoryRes> recurringHistory) {
        this.hotelType = hotelType;
        this.owner = owner;
        this.bookings = bookings;
        this.billingRules = billingRules;
        this.billingDates = billingDates;
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

        int noOfActiveTenants = bookings.size();

        boolean isPostpaid = BillingModel.POSTPAID.name().equals(billingRules.getBillingModel());

        Date today = new Date();
        Date cycleStartDate;
        Date cycleEndDate;
        Date cycleDate;
        int startDay = billingRules.getBillingStartDate();
        int endDay;
        if (billingDates != null){
            cycleStartDate = billingDates.currentBillStartDate();
            cycleEndDate = billingDates.currentBillEndDate();
        } else {
            cycleDate = today;
            if (isPostpaid) {
                cycleDate = Utils.getPreviousMonthDate(today);
            }

            int month = Utils.getCurrentMonth(cycleDate);
            int year = Utils.getCurrentYear(cycleDate);

            cycleStartDate = Utils.getDateFromDay(startDay, month, year);
            cycleEndDate = Utils.getEndDate(startDay, month, year);
        }
        endDay = Utils.calculateEndDay(startDay, cycleStartDate);

        int invoiceAboutToBeGenerated = 0;
        if (!bookings.isEmpty()){
            if (!isPostpaid){
                invoiceAboutToBeGenerated = (int) bookings.stream()
                        .filter(customer -> Utils.isEligibleForInvoice(customer, cycleStartDate))
                        .count();
            } else {
                invoiceAboutToBeGenerated = bookings.size();
            }
        }

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
                    billingRules.getBillingStartDate(), latestRecurringTracker, cycleStartDate);
        }

        return new RecurringTrackerRes(hostel.getHostelId(), hostelType, hostelName, Utils.getInitials(hostelName),
                hostel.getMobile(), hostel.getEmailId(), hostel.getHouseNo(), hostel.getStreet(), hostel.getLandmark(),
                hostel.getCity(), hostel.getState(), hostel.getCountry(), hostel.getPincode(), fullAddress, hostel.getMainImage(),
                ownerInfo, noOfActiveTenants, invoiceAboutToBeGenerated, billingRules.getTypeOfBilling(), billingRules.getBillingModel(),
                startDay, endDay, Utils.dateToString(cycleStartDate), Utils.dateToString(cycleEndDate),
                Utils.dateToString(lastRecurringDate), Utils.dateToString(nextRecurringDate), isSubscriptionActive,
                recurringStatus, page, size, paginatedRecurringTrackers.getTotalElements(), paginatedRecurringTrackers.getTotalPages(),
                recurringHistory);
    }
}
