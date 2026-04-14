package com.smartstay.console.Mapper.customers;

import com.smartstay.console.Mapper.users.UserOnerInfoMapper;
import com.smartstay.console.dao.*;
import com.smartstay.console.dto.hostel.BillingDates;
import com.smartstay.console.ennum.BillingModel;
import com.smartstay.console.responses.customers.CustomerRecurringResponse;
import com.smartstay.console.responses.hostels.OwnerInfo;
import com.smartstay.console.utils.Utils;

import java.util.Date;
import java.util.function.Function;

public class CustomerRecurringMapper implements Function<Customers, CustomerRecurringResponse> {

    Users owner;
    HotelType hotelType;
    HostelV1 hostel;
    BillingRules hostelBillingRules;
    BillingDates billingDates;
    CustomerRecurringTracker customerRecurringTracker;
    Agent trackerCreatedAgent;

    public CustomerRecurringMapper(Users owner,
                                   HotelType hotelType,
                                   HostelV1 hostel,
                                   BillingRules hostelBillingRules,
                                   BillingDates billingDates,
                                   CustomerRecurringTracker customerRecurringTracker,
                                   Agent trackerCreatedAgent) {
        this.owner = owner;
        this.hotelType = hotelType;
        this.hostel = hostel;
        this.hostelBillingRules = hostelBillingRules;
        this.billingDates = billingDates;
        this.customerRecurringTracker = customerRecurringTracker;
        this.trackerCreatedAgent = trackerCreatedAgent;
    }

    @Override
    public CustomerRecurringResponse apply(Customers customers) {

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
        Date cycleStartDate = today;

        Date joinedDate = customers.getJoiningDate() != null ? customers.getJoiningDate() : customers.getExpJoiningDate();
        if (joinedDate != null){
            startDay = Utils.getDayOfMonth(joinedDate);
            if (billingDates != null){
                cycleStartDate = billingDates.currentBillStartDate();
                endDay = Utils.calculateEndDay(startDay, cycleStartDate);
            } else {
                if (isPostpaid) {
                    cycleStartDate = Utils.getPreviousMonthDate(today);
                }
                endDay = Utils.calculateEndDay(startDay, cycleStartDate);
            }
        }

        boolean recurringStatus = false;
        Integer recurringDay = startDay;
        String lastRecurringDate = null;
        String recurringMode = null;
        String recurringCreatedAtDate = null;
        String recurringCreatedAtTime = null;
        String createdBy = null;

        if (customerRecurringTracker != null){
            Date createdAt = customerRecurringTracker.getCreatedAt();

            recurringStatus = Utils.isSameBillingCycle(startDay,
                    customerRecurringTracker, cycleStartDate);

            recurringMode = customerRecurringTracker.getMode();

            recurringCreatedAtDate = Utils.dateToString(createdAt);
            recurringCreatedAtTime = Utils.dateToTime(createdAt);

            if (trackerCreatedAgent != null){
                createdBy = Utils.getFullName(trackerCreatedAgent.getFirstName(), trackerCreatedAgent.getLastName());
            }

            lastRecurringDate = Utils.dateToString(createdAt);
        }

        return new CustomerRecurringResponse(customers.getCustomerId(), firstName, lastName, fullName,
                initials, customers.getMobile(), customers.getEmailId(), customers.getProfilePic(),
                customers.getHouseNo(), customers.getStreet(), customers.getLandmark(), customers.getPincode(),
                customers.getCity(), customers.getState(), customers.getCountry(), fullAddress,
                customers.getCustomerBedStatus(), customers.getCurrentStatus(), joiningDate, expJoiningDate,
                customers.getHostelId(), hostelType, hostelName, hostelInitials, hostelMobile, hostelEmailId,
                hostelHouseNo, hostelStreet, hostelLandmark, hostelCity, hostelState, hostelCountry,
                hostelPincode, hostelFullAddress, mainImage, ownerInfo, startDay, endDay, billingType,
                billingModel, isSubscriptionActive, recurringStatus, recurringDay, lastRecurringDate,
                recurringMode, recurringCreatedAtDate, recurringCreatedAtTime, createdBy);
    }
}
