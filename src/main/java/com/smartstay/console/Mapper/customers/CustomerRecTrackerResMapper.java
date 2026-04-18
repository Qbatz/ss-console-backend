package com.smartstay.console.Mapper.customers;

import com.smartstay.console.Mapper.users.UserOnerInfoMapper;
import com.smartstay.console.dao.*;
import com.smartstay.console.dto.hostel.BillingDates;
import com.smartstay.console.ennum.BillingModel;
import com.smartstay.console.responses.customers.CustomerRecHistoryRes;
import com.smartstay.console.responses.customers.CustomerRecTrackerRes;
import com.smartstay.console.responses.hostels.OwnerInfo;
import com.smartstay.console.utils.Utils;
import org.springframework.data.domain.Page;

import java.util.Date;
import java.util.List;
import java.util.function.Function;

public class CustomerRecTrackerResMapper implements Function<Customers, CustomerRecTrackerRes> {

    Users owner;
    HotelType hotelType;
    HostelV1 hostel;
    BillingRules hostelBillingRules;
    BillingDates billingDates;
    CustomerRecurringTracker customerRecurringTracker;
    int page;
    int size;
    Page<CustomerRecurringTracker> paginatedRecurringTrackers;
    List<CustomerRecHistoryRes> recurringHistory;

    public CustomerRecTrackerResMapper(Users owner,
                                       HotelType hotelType,
                                       HostelV1 hostel,
                                       BillingRules hostelBillingRules,
                                       BillingDates billingDates,
                                       CustomerRecurringTracker customerRecurringTracker,
                                       int page, int size,
                                       Page<CustomerRecurringTracker> paginatedRecurringTrackers,
                                       List<CustomerRecHistoryRes> recurringHistory) {
        this.owner = owner;
        this.hotelType = hotelType;
        this.hostel = hostel;
        this.hostelBillingRules = hostelBillingRules;
        this.billingDates = billingDates;
        this.customerRecurringTracker = customerRecurringTracker;
        this.page = page;
        this.size = size;
        this.paginatedRecurringTrackers = paginatedRecurringTrackers;
        this.recurringHistory = recurringHistory;
    }

    @Override
    public CustomerRecTrackerRes apply(Customers customers) {

        String firstName = customers.getFirstName();
        String lastName = customers.getLastName();
        String fullName = Utils.getFullName(firstName, lastName);
        String initials = Utils.getInitials(firstName, lastName);
        String fullAddress = Utils.buildFullAddress(customers);
        String joiningDate = Utils.dateToString(customers.getJoiningDate());
        String expJoiningDate = Utils.dateToString(customers.getExpJoiningDate());

        String hostelName = null;
        String hostelInitials = null;
        String hostelMobile = null;
        String hostelEmailId = null;
        String hostelHouseNo = null;
        String hostelStreet = null;
        String hostelLandmark = null;
        String hostelCity = null;
        String hostelState = null;
        int hostelCountry = 0;
        int hostelPincode = 0;
        String hostelFullAddress = null;
        String mainImage = null;
        boolean isSubscriptionActive = false;
        if (hostel != null) {
            hostelName = hostel.getHostelName();
            hostelInitials = Utils.getInitials(hostelName);
            hostelMobile = hostel.getMobile();
            hostelEmailId = hostel.getEmailId();
            hostelHouseNo = hostel.getHouseNo();
            hostelStreet = hostel.getStreet();
            hostelLandmark = hostel.getLandmark();
            hostelCity = hostel.getCity();
            hostelState = hostel.getState();
            hostelCountry = hostel.getCountry();
            hostelPincode = hostel.getPincode();
            hostelFullAddress = Utils.buildFullAddress(hostel);
            mainImage = hostel.getMainImage();

            HostelPlan hostelPlan = hostel.getHostelPlan();
            if (hostelPlan != null && hostelPlan.getCurrentPlanEndsAt() != null) {
                isSubscriptionActive = Utils.compareWithTwoDates(
                        hostelPlan.getCurrentPlanEndsAt(), new Date()) >= 0;
            }
        }

        String hostelType = null;
        if (hotelType != null){
            hostelType = hotelType.getType();
        }

        OwnerInfo ownerInfo = null;
        if (owner != null){
            ownerInfo = new UserOnerInfoMapper().apply(owner);
        }

        boolean isPostpaid = false;
        String billingType = null;
        String billingModel = null;
        if (hostelBillingRules != null){
            billingType = hostelBillingRules.getTypeOfBilling();
            billingModel = hostelBillingRules.getBillingModel();
            isPostpaid = BillingModel.POSTPAID.name().equals(billingModel);
        }

        Date today = new Date();
        int startDay = 1;
        int endDay = Utils.calculateEndDay(startDay, today);
        Date startDate = today;
        Date endDate = null;

        Date joinedDate = customers.getJoiningDate() != null ? customers.getJoiningDate() : customers.getExpJoiningDate();
        if (joinedDate != null){
            startDay = Utils.getDayOfMonth(joinedDate);

            if (billingDates != null){
                startDate = billingDates.currentBillStartDate();
                endDate = billingDates.currentBillEndDate();
                endDay = Utils.calculateEndDay(startDay, startDate);
            } else {
                Date cycleDate = today;
                if (isPostpaid) {
                    cycleDate = Utils.getPreviousMonthDate(today);
                }

                int month = Utils.getCurrentMonth(cycleDate);
                int year = Utils.getCurrentYear(cycleDate);

                startDate = Utils.getDateFromDay(startDay, month, year);
                endDate = Utils.getEndDate(startDay, month, year);

                endDay = Utils.calculateEndDay(startDay, cycleDate);
            }
        }

        boolean recurringStatus = false;
        Date lastRecurringDate = null;
        Date nextRecurringDate = Utils.getNextMonthDate(startDate);

        if (customerRecurringTracker != null){
            recurringStatus = Utils.isSameBillingCycle(startDay, customerRecurringTracker, startDate);
            lastRecurringDate = Utils.getDateFromDay(customerRecurringTracker.getCreationDay(),
                    customerRecurringTracker.getCreationMonth(), customerRecurringTracker.getCreationYear());
        }

        return new CustomerRecTrackerRes(customers.getCustomerId(), firstName, lastName, fullName,
                initials, customers.getMobile(), customers.getEmailId(), customers.getProfilePic(),
                customers.getHouseNo(), customers.getStreet(), customers.getLandmark(), customers.getPincode(),
                customers.getCity(), customers.getState(), customers.getCountry(), fullAddress,
                customers.getCustomerBedStatus(), customers.getCurrentStatus(), joiningDate, expJoiningDate,
                customers.getHostelId(), hostelType, hostelName, hostelInitials, hostelMobile, hostelEmailId,
                hostelHouseNo, hostelStreet, hostelLandmark, hostelCity, hostelState, hostelCountry,
                hostelPincode, hostelFullAddress, mainImage, ownerInfo, startDay, endDay, billingType,
                billingModel, Utils.dateToString(startDate), Utils.dateToString(endDate),
                Utils.dateToString(lastRecurringDate), Utils.dateToString(nextRecurringDate), isSubscriptionActive,
                recurringStatus,  page, size, paginatedRecurringTrackers.getTotalElements(),
                paginatedRecurringTrackers.getTotalPages(), recurringHistory);
    }
}
