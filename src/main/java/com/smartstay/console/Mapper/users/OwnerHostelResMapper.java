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

        String fullAddress = buildFullAddress(hostel);

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

    private static String buildFullAddress(HostelV1 hostelV1) {
        StringBuilder fullAddress = new StringBuilder();

        if (hostelV1.getHouseNo() != null && !hostelV1.getHouseNo().trim().equalsIgnoreCase("")) {
            fullAddress.append(hostelV1.getHouseNo());
        }
        if (hostelV1.getHouseNo() != null && !hostelV1.getHouseNo().trim().equalsIgnoreCase("")
                && hostelV1.getStreet() != null) {
            fullAddress.append(", ");
            fullAddress.append(hostelV1.getStreet());
        }
        else {
            fullAddress.append(hostelV1.getStreet());
        }
        if (hostelV1.getCity() != null) {
            if (fullAddress.isEmpty()) {
                fullAddress.append(hostelV1.getCity());
            }
            else {
                fullAddress.append(", ");
                fullAddress.append(hostelV1.getCity());
            }
        }
        if (hostelV1.getState() != null) {
            if (fullAddress.isEmpty()) {
                fullAddress.append(hostelV1.getState());
            }
            else {
                fullAddress.append(", ");
                fullAddress.append(hostelV1.getState());
            }
        }
        return fullAddress.toString();
    }
}
