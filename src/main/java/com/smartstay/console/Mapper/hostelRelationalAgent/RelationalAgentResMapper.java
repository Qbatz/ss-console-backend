package com.smartstay.console.Mapper.hostelRelationalAgent;

import com.smartstay.console.dao.Agent;
import com.smartstay.console.dao.HostelPlan;
import com.smartstay.console.dao.HostelRelationalAgent;
import com.smartstay.console.dao.HostelV1;
import com.smartstay.console.responses.hostelRelationalAgent.RelationalAgentResponse;
import com.smartstay.console.utils.Utils;

import java.util.Date;
import java.util.function.Function;

public class RelationalAgentResMapper implements Function<HostelRelationalAgent, RelationalAgentResponse> {

    HostelV1 hostel;
    Agent relationalAgent;
    Agent createdByAgent;

    public RelationalAgentResMapper(HostelV1 hostel,
                                    Agent relationalAgent,
                                    Agent createdByAgent) {
        this.hostel = hostel;
        this.relationalAgent = relationalAgent;
        this.createdByAgent = createdByAgent;
    }

    @Override
    public RelationalAgentResponse apply(HostelRelationalAgent hostelRelationalAgent) {

        String hostelName = null;
        String initials = null;
        String mobile = null;
        String houseNo = null;
        String street = null;
        String landmark = null;
        String city = null;
        String state = null;
        int country = 0;
        int pincode = 0;
        String fullAddress = null;
        String mainImage = null;

        HostelPlan hostelPlan = null;
        String planCode = null;
        String planName = null;
        String planEndsAtDate = null;
        String planEndsAtTime = null;
        long expiringInDays = 0;
        boolean aboutToExpire = false;

        Date today = new Date();

        if (hostel != null){
            hostelName = hostel.getHostelName();
            if (hostelName != null) {
                initials = Utils.getInitials(hostel.getHostelName());
            }
            mobile = hostel.getMobile();
            houseNo = hostel.getHouseNo();
            street = hostel.getStreet();
            landmark = hostel.getLandmark();
            city = hostel.getCity();
            state = hostel.getState();
            country = hostel.getCountry();
            pincode = hostel.getPincode();
            fullAddress = Utils.buildFullAddress(hostel);
            mainImage = hostel.getMainImage();
            hostelPlan = hostel.getHostelPlan();
        }

        if (hostelPlan != null){
            planCode = hostelPlan.getCurrentPlanCode();
            planName = hostelPlan.getCurrentPlanName();
            Date expiresAt = hostelPlan.getCurrentPlanEndsAt();
            if (expiresAt != null) {
                planEndsAtDate = Utils.dateToString(expiresAt);
                planEndsAtTime = Utils.dateToTime(expiresAt);
                expiringInDays = Utils.findNumberOfDays(today, expiresAt);
                if (expiringInDays >= 0 && expiringInDays <= 10){
                    aboutToExpire = true;
                }
            }
        }

        String relationalAgentName = null;
        if (relationalAgent != null){
            relationalAgentName = Utils.getFullName(relationalAgent.getFirstName(), relationalAgent.getLastName());
        }

        String createdByAgentName = null;
        if (createdByAgent != null){
            createdByAgentName = Utils.getFullName(createdByAgent.getFirstName(), createdByAgent.getLastName());
        }

        return new RelationalAgentResponse(hostelRelationalAgent.getId(), hostelRelationalAgent.getHostelId(),
                hostelName, initials, mobile, houseNo, street, landmark, city, state, country, pincode, fullAddress,
                mainImage, planCode, planName, planEndsAtDate, planEndsAtTime, expiringInDays, aboutToExpire,
                hostelRelationalAgent.getAgentId(), relationalAgentName, hostelRelationalAgent.getReason().name(),
                hostelRelationalAgent.getComments(), createdByAgentName, Utils.dateToString(hostelRelationalAgent.getCreatedAt()),
                Utils.dateToTime(hostelRelationalAgent.getCreatedAt()));
    }
}
