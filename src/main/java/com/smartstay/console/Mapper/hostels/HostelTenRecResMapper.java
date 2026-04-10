package com.smartstay.console.Mapper.hostels;

import com.smartstay.console.Mapper.users.UserOnerInfoMapper;
import com.smartstay.console.dao.*;
import com.smartstay.console.responses.customers.CustomerRecurringResponse;
import com.smartstay.console.responses.hostels.HostelTenRecResponse;
import com.smartstay.console.responses.hostels.OwnerInfo;
import com.smartstay.console.utils.Utils;

import java.util.Date;
import java.util.List;
import java.util.function.Function;

public class HostelTenRecResMapper implements Function<HostelV1, HostelTenRecResponse> {

    BillingRules billingRules;
    Users owner;
    HotelType hotelType;
    List<BookingsV1> bookings;
    List<CustomerRecurringResponse> tenantList;

    public HostelTenRecResMapper(BillingRules billingRules,
                                 Users owner,
                                 HotelType hotelType,
                                 List<BookingsV1> bookings,
                                 List<CustomerRecurringResponse> tenantList) {
        this.billingRules = billingRules;
        this.owner = owner;
        this.hotelType = hotelType;
        this.bookings = bookings;
        this.tenantList = tenantList;
    }

    @Override
    public HostelTenRecResponse apply(HostelV1 hostel) {

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

        String billingModel = null;
        String billingType = null;
        if (billingRules != null){
            billingModel = billingRules.getBillingModel();
            billingType = billingRules.getTypeOfBilling();
        }

        int noOfActiveTenants = bookings != null ? bookings.size() : 0;

        int invoiceAboutToBeGenerated = tenantList.size();

        return new HostelTenRecResponse(hostel.getHostelId(), hostelType, hostelName, Utils.getInitials(hostelName),
                hostel.getMobile(), hostel.getEmailId(), hostel.getHouseNo(), hostel.getStreet(), hostel.getLandmark(),
                hostel.getCity(), hostel.getState(), hostel.getCountry(), hostel.getPincode(), fullAddress, hostel.getMainImage(),
                ownerInfo, noOfActiveTenants, invoiceAboutToBeGenerated, billingType, billingModel, isSubscriptionActive,
                tenantList
        );
    }
}
