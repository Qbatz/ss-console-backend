package com.smartstay.console.responses.beds;

import com.smartstay.console.responses.customers.CustomerRes;
import com.smartstay.console.responses.hostels.HostelInfoRes;
import com.smartstay.console.responses.users.UserActivitiesResponse;

import java.util.List;

public record BedsInfoResponse(Integer bedId,
                               String bedName,
                               Integer roomId,
                               String roomName,
                               Integer floorId,
                               String floorName,
                               boolean isBooked,
                               double rentAmount,
                               String currentStatus,
                               String freeFromDate,
                               CustomerRes customer,
                               HostelInfoRes hostel,
                               List<UserActivitiesResponse> bedActivities) {
}
