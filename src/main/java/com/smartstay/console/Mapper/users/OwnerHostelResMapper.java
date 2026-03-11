package com.smartstay.console.Mapper.users;

import com.smartstay.console.dao.HostelPlan;
import com.smartstay.console.dao.HostelV1;
import com.smartstay.console.dao.HotelType;
import com.smartstay.console.responses.hostels.HostelPlanResponse;
import com.smartstay.console.responses.hostels.OwnerHostelResponse;
import com.smartstay.console.utils.Utils;

import java.util.Date;
import java.util.function.Function;

public class OwnerHostelResMapper implements Function<HostelV1, OwnerHostelResponse> {

    HotelType hotelType;

    public OwnerHostelResMapper(HotelType hotelType) {
        this.hotelType = hotelType;
    }

    @Override
    public OwnerHostelResponse apply(HostelV1 hostel) {

        String hostelName = hostel.getHostelName() != null ? hostel.getHostelName().trim() : null;

        String fullAddress = Utils.buildFullAddress(hostel);

        HostelPlan hostelPlan = hostel.getHostelPlan();
        boolean isSubscriptionActive;
        if (Utils.compareWithTwoDates(hostelPlan.getCurrentPlanEndsAt(), new Date()) < 0) {
            isSubscriptionActive = false;
        }
        else {
            isSubscriptionActive = true;
        }

        HostelPlanResponse hostelPlanRes = new HostelPlanResponse(hostelPlan.getCurrentPlanCode(),
                hostelPlan.getCurrentPlanName(), Utils.dateToString(hostelPlan.getCurrentPlanStartsAt()),
                Utils.dateToString(hostelPlan.getCurrentPlanEndsAt()), hostelPlan.getPaidAmount(),
                isSubscriptionActive);

        String hostelType = null;
        if (hotelType != null){
            hostelType = hotelType.getType();
        }

        return new OwnerHostelResponse(hostel.getHostelId(), hostelType, hostelName, Utils.getInitials(hostelName),
                hostel.getMobile(), hostel.getHouseNo(), hostel.getStreet(), hostel.getLandmark(), hostel.getCity(),
                hostel.getState(), hostel.getCountry(), hostel.getPincode(), fullAddress, hostel.getMainImage(),
                Utils.dateToString(hostel.getCreatedAt()), hostelPlanRes);
    }
}
